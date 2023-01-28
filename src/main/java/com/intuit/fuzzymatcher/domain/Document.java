package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.function.ScoringFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * The primary object for matching. The required attribute is a unique key and elements
 * <p>
 * Configurable attributes
 * <ul>
 * <li>elements - A set of Element object to match against</li>
 * <li>threshold - Value above which documents are considered a match, default 0.5</li>
 * </ul>
 */
public class Document implements Matchable {
    private Document(String key, Set<Element> elements, double threshold) {
        this.key = key;
        this.elements = elements;
        this.threshold = threshold;
    }

    private String key;
    private Set<Element> elements;
    private Set<Element> preProcessedElement;
    private double threshold;
    private Boolean source;

    private static final BiFunction<Match, List<Score>, Score> DEFAULT_DOCUMENT_SCORING = ScoringFunction.getExponentialWeightedAverageScore();

    public String getKey() {
        return key;
    }

    public Set<Element> getElements() {
        return elements;
    }

    public Set<Element> getPreProcessedElement() {
        if (this.preProcessedElement == null) {
            this.preProcessedElement = getDistinctNonEmptyElements().collect(Collectors.toSet());
        }
        return preProcessedElement;
    }

    public double getThreshold() {
        return threshold;
    }

    public Stream<Element> getDistinctElements() {
        return this.elements.stream()
                .filter(distinctByKey(Element::getPreprocessedValueWithType));
    }

    public Stream<Element> getDistinctNonEmptyElements() {
        return getDistinctElements()
                .filter(m -> {
                    if (m.getPreProcessedValue() instanceof String) {
                        return !StringUtils.isEmpty(m.getPreProcessedValue().toString());
                    } else
                        return m.getPreProcessedValue() != null;
                });
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public double getWeightedChildCount(Matchable other) {
        if (other instanceof Document) {
            Document o = (Document) other;
            Map<ElementClassification, Double> childrenTypeAndWeight = this.getPreProcessedElement().stream()
                    .collect(Collectors.toMap(Element::getElementClassification, Element::getWeight, Double::sum));
            Map<ElementClassification, Double> oChildrenTypeAndWeight = o.getPreProcessedElement().stream()
                    .collect(Collectors.toMap(Element::getElementClassification, Element::getWeight, Double::sum));
            double val = CollectionUtils.union(childrenTypeAndWeight.keySet(), oChildrenTypeAndWeight.keySet()).stream()
                    .map(element -> Optional.ofNullable(childrenTypeAndWeight.get(element))
                            .orElseGet(() -> oChildrenTypeAndWeight.get(element)))
                    .reduce(Double::sum)
                    .orElse(0.0);
            return val;
        }
        return 0.0;
    }

    @Override
    public double getUnmatchedChildWeight(Matchable other) {
        if (other instanceof Document) {
            Document o = (Document) other;
            Map<ElementClassification, Double> childrenTypeAndWeight = this.getPreProcessedElement().stream()
                    .collect(Collectors.toMap(Element::getElementClassification, Element::getWeight, Double::sum));
            Map<ElementClassification, Double> oChildrenTypeAndWeight = o.getPreProcessedElement().stream()
                    .collect(Collectors.toMap(Element::getElementClassification, Element::getWeight, Double::sum));
            double val = CollectionUtils.disjunction(childrenTypeAndWeight.keySet(), oChildrenTypeAndWeight.keySet()).stream()
                    .map(element -> Optional.ofNullable(childrenTypeAndWeight.get(element))
                            .orElseGet(() -> oChildrenTypeAndWeight.get(element)))
                    .reduce(Double::sum)
                    .orElse(0.0);
            return val;
        }
        return 0.0;
    }

    @Override
    public BiFunction<Match, List<Score>, Score> getScoringFunction() {
        return DEFAULT_DOCUMENT_SCORING;
    }

    @Override
    public double getWeight() {
        return 1.0;
    }

    public Boolean isSource() {
        return source;
    }

    public void setSource(Boolean source) {
        this.source = source;
    }

    public static class Builder {
        private String key;
        private Set<Element> elements;
        private double threshold = 0.5;

        public Builder(String key) {
            this.key = key;
        }

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder addElement(Element element) {
            if (this.elements == null || this.elements.isEmpty()) {
                this.elements = new HashSet<>();
            }
            this.elements.add(element);
            return this;
        }

        public Document createDocument() {
            Document doc = new Document(key, elements, threshold);
            doc.elements.stream().forEach(element -> element.setDocument(doc));
            return doc;
        }
    }

    @Override
    public String toString() {
        return "{" + getOrderedElements(elements) + "}";
    }

    public List<Element> getOrderedElements(Set<Element> elements) {
        return elements.stream().sorted(Comparator.comparing(ele -> ele.getElementClassification().getElementType()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        return key.equals(document.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
