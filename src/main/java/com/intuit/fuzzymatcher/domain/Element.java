package com.intuit.fuzzymatcher.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.function.PreProcessFunction.toLowerCase;
import static com.intuit.fuzzymatcher.function.PreProcessFunction.trim;

/**
 * <p>
 * This class represent the string "value" against which match are run.
 * <p>
 * Configurable attributes
 * <ul>
 * <li>type - The ElementType for the value. This determines the functions applied at different steps of the match</li>
 * <li>weight - Used in scoring function to increase the Document score for an Element. Default is 1.0 for all elements</li>
 * <li>threshold - Value above which elements are considered a match, default 0.3</li>
 * <li>neighborhoodRange - Relevant for NEAREST_NEIGHBORS MatchType. Defines how close should the value be, to be considered a match (default 0.9) </li>
 * <li>preProcessFunction - Function to pre-process the value. If this is not set, the function defined in ElementType is used </li>
 * <li>tokenizerFunction - Function to break values into tokens. If this is not set, the function defined in ElementType is used </li>
 * <li>matchType - MatchType used. If this is not set, the type defined in ElementType is used </li>
 * </ul>
 */
public class Element<T> implements Matchable {

    private T value;
    private double weight;
    private double threshold;
    private double neighborhoodRange;
    private ElementClassification elementClassification;
    private Document document;
    private Function<T, T> preProcessFunction;
    private Function<Element<T>, Stream<Token>> tokenizerFunction;
    private List<Token> tokens;
    private MatchType matchType;

    private T preProcessedValue;

    public Element(ElementType type, String variance, T value, double weight, double threshold,
                   double neighborhoodRange, Function<T, T> preProcessFunction,
                   Function<Element<T>, Stream<Token>> tokenizerFunction, MatchType matchType) {
        this.weight = weight;
        this.elementClassification = new ElementClassification(type, variance);
        this.value = value;
        this.threshold = threshold;
        this.preProcessFunction = preProcessFunction == null ? type.getPreProcessFunction() : preProcessFunction;
        this.tokenizerFunction = tokenizerFunction == null ? type.getTokenizerFunction() : tokenizerFunction;
        this.matchType = matchType == null ? type.getMatchType() : matchType;
        this.neighborhoodRange = neighborhoodRange;
    }

    public ElementClassification getElementClassification() {
        return elementClassification;
    }

    public T getValue() {
        return value;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getNeighborhoodRange() {
        return neighborhoodRange;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setPreProcessedValue(T preProcessedValue) {
        this.preProcessedValue = preProcessedValue;
    }

    public Function<T, T> getPreProcessFunction() {
        return this.preProcessFunction;
    }

    public T getPreProcessedValue() {
        if (this.preProcessedValue == null) {
            if (this.value instanceof String) {
                // Default String pre-processing
                Function<String, String> preProcessingFunc = (Function<String, String>) getPreProcessFunction();
                setPreProcessedValue((T) preProcessingFunc.andThen(trim()).andThen(toLowerCase()).apply((String) this.value));
            } else {
                setPreProcessedValue(getPreProcessFunction().apply(this.value));
            }
        }
        return this.preProcessedValue;
    }

    public AbstractMap.SimpleEntry getPreprocessedValueWithType() {
        return new AbstractMap.SimpleEntry(this.getElementClassification(), this.getPreProcessedValue());
    }

    public Function<Element<T>, Stream<Token>> getTokenizerFunction() {
        return this.tokenizerFunction;
    }

    public MatchType getMatchType() {
        return this.matchType;
    }

    public List<Token> getTokens() {
        if (this.tokens == null) {
            this.tokens = getTokenizerFunction().apply(this).distinct().collect(Collectors.toList());
        }
        return this.tokens;
    }

    public double getScore(Integer matchingCount, Element other) {
        return ((double)matchingCount / getWeightedChildCount(other));
    }


    /**
     * This gets the Max number of tokens present between matching Elements.
     * For Elements that do not have a balanced set of tokens, it can push the score down.
     * @return
     */
    @Override
    public double getWeightedChildCount(Matchable other) {
        if (other instanceof Element) {
            Element<T> o = (Element<T>) other;
            return Math.max(this.getTokens().size(), o.getTokens().size());
        }
        return 0;
    }

    @Override
    public double getUnmatchedChildWeight(Matchable other) {
        if (other instanceof Element) {
            Element<T> o = (Element<T>) other;
            long emptyChildren = this.getTokens().stream()
                    .filter(token -> token == null || StringUtils.isEmpty(token.getValue().toString()))
                    .count();
            long oEmptyChildren = o.getTokens().stream()
                    .filter(token -> token == null || StringUtils.isEmpty(token.getValue().toString()))
                    .count();
            return Math.max(emptyChildren, oEmptyChildren);
        }
        return 0;
    }

    @Override
    public BiFunction<Match, List<Score>, Score> getScoringFunction() {
        return null;
    }

    public static class Builder<T> {
        private ElementType type;
        private String variance;
        private T value;
        private double weight = 1.0;
        private double threshold = 0.3;
        private double neighborhoodRange = 0.9;
        private Function<T, T> preProcessFunction;
        private MatchType matchType;

        private Function<Element<T>, Stream<Token>> tokenizerFunction;

        public Builder setType(ElementType type) {
            this.type = type;
            return this;
        }

        public Builder setVariance(String variance) {
            this.variance = variance;
            return this;
        }

        public Builder setValue(T value) {
            this.value = value;
            return this;
        }

        public Builder setWeight(double weight) {
            this.weight = weight;
            return this;
        }

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setNeighborhoodRange(double neighborhoodRange) {
            this.neighborhoodRange = neighborhoodRange;
            return this;
        }

        public Builder setPreProcessingFunction(Function<T, T> preProcessingFunction) {
            this.preProcessFunction = preProcessingFunction;
            return this;
        }


        public Builder setTokenizerFunction(Function<Element<T>, Stream<Token>> tokenizerFunction) {
            this.tokenizerFunction = tokenizerFunction;
            return this;
        }

        public Builder setMatchType(MatchType matchType) {
            this.matchType = matchType;
            return this;
        }


        public Element createElement() {
            return new Element<T>(type, variance, value, weight, threshold, neighborhoodRange, preProcessFunction, tokenizerFunction, matchType);
        }
    }

    @Override
    public String toString() {
        return "Element{" +
                "value=" + value +
                ", classification=" + elementClassification +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Element element = (Element) o;

        if (value != null ? !value.equals(element.value) : element.value != null) return false;
        if (elementClassification != null ? !elementClassification.equals(element.elementClassification) : element.elementClassification != null)
            return false;
        return document != null ? document.equals(element.document) : element.document == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (elementClassification != null ? elementClassification.hashCode() : 0);
        result = 31 * result + (document != null ? document.hashCode() : 0);
        return result;
    }
}

