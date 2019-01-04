package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.function.ScoringFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 * <li>scoringFunction - Function used to aggregate scores of matching elements, default ExponentialWeightedAverageScore</li>
 * </ul>
 */
public class Document implements Matchable {
    private Document(String key, Set<Element> elements, double threshold, Function<Match, Score> scoringFunction) {
        this.key = key;
        this.elements = elements;
        this.threshold = threshold;
        this.scoringFunction = scoringFunction;
    }

    private String key;
    private Set<Element> elements;
    private Set<Element> preProcessedElement;
    private double threshold;
    private Function<Match, Score> scoringFunction;
    private Boolean source;

    private static final Function<Match, Score> DEFAULT_DOCUMENT_SCORING = ScoringFunction.getExponentialWeightedAverageScore();

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
                .filter(m -> !StringUtils.isEmpty(m.getPreProcessedValue()));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public long getChildCount(Matchable other) {
        if (other instanceof Document) {
            Document o = (Document) other;
            List<ElementType> childrenType =  this.getPreProcessedElement().stream()
                    .map(Element::getType).collect(Collectors.toList());
            List<ElementType> oChildrenType =  o.getPreProcessedElement().stream()
                    .map(Element::getType).collect(Collectors.toList());
            return CollectionUtils.union(childrenType, oChildrenType).size();
        }
        return 0;

    }

    @Override
    public long getUnmatchedChildCount(Matchable other) {
        if (other instanceof Document) {
            Document o = (Document) other;
            List<ElementType> childrenType =  this.getPreProcessedElement().stream()
                    .map(Element::getType).collect(Collectors.toList());
            List<ElementType> oChildrenType =  o.getPreProcessedElement().stream()
                    .map(Element::getType).collect(Collectors.toList());
            return CollectionUtils.disjunction(childrenType, oChildrenType).size();
        }
        return 0;
    }

    @Override
    public Function<Match, Score> getScoringFunction() {
        return this.scoringFunction != null ? this.scoringFunction : DEFAULT_DOCUMENT_SCORING;
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
        private Function<Match, Score> scoringFunction;

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

        public Builder setScoringFunction(Function<Match, Score> scoringFunction) {
            this.scoringFunction = scoringFunction;
            return this;
        }

        public Document createDocument() {
            Document doc = new Document(key, elements, threshold, scoringFunction);
            doc.elements.stream().forEach(element -> element.setDocument(doc));
            return doc;
        }
    }

    @Override
    public String toString() {
        return "{" + getOrderedElements(elements) + "}";
    }

    public List<Element> getOrderedElements(Set<Element> elements) {
        return elements.stream().sorted(Comparator.comparing(Element::getType)).collect(Collectors.toList());
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
