package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.function.ScoringFunction;
import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
 * <li>scoringFunction - Function used to aggregate scores of matching elements, default ExponentialWeightedAverageScore</li>
 * <li>preProcessFunction - Function to pre-process the value. If this is not set, the function defined in ElementType is used </li>
 * <li>tokenizerFunction - Function to break values into tokens. If this is not set, the function defined in ElementType is used </li>
 * <li>similarityMatchFunction - Function to compare tokens. If this is not set, the function defined in ElementType is used </li>
 * </ul>
 */
public class Element implements Matchable {

    private Object value;
    private double weight;
    private double threshold;
    private ElementClassification elementClassification;
    private Document document;
    private Function<Object, Object> preProcessFunction;
    private Function<Element, Stream<Token>> tokenizerFunction;
    private BiFunction<Token, Token, Double> similarityMatchFunction;
    private Function<Match, Score> scoringFunction;
    private List<Token> tokens;

    private Object preProcessedValue;

    private static final Function<Match, Score> DEFAULT_ELEMENT_SCORING = ScoringFunction.getSimpleAverageScore();

    public Element(ElementType type, String variance, Object value, double weight, double threshold,
                   Function<Object, Object> preProcessFunction,
                   Function<Element, Stream<Token>> tokenizerFunction,
                   BiFunction<Token, Token, Double> similarityMatchFunction, Function<Match, Score> scoringFunction,
                   Function<List<Token>, Stream<Match<Token>>> matchOptimizerFunction) {
        this.weight = weight;
        this.elementClassification = new ElementClassification(type, variance,
                matchOptimizerFunction == null ? type.getMatchOptimizerFunction() : matchOptimizerFunction);
        this.value = value;
        this.threshold = threshold;
        this.preProcessFunction = preProcessFunction == null ? type.getPreProcessFunction() : preProcessFunction;
        this.tokenizerFunction = tokenizerFunction == null ? type.getTokenizerFunction() : tokenizerFunction;
        this.similarityMatchFunction = similarityMatchFunction == null ? type.getSimilarityMatchFunction() : similarityMatchFunction;
        this.scoringFunction = scoringFunction != null ? this.scoringFunction : DEFAULT_ELEMENT_SCORING;
    }

    public ElementClassification getElementClassification() {
        return elementClassification;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    public double getThreshold() {
        return threshold;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setPreProcessedValue(Object preProcessedValue) {
        this.preProcessedValue = preProcessedValue;
    }

    public Function<Object, Object> getPreProcessFunction() {
        return this.preProcessFunction;
    }

    public Object getPreProcessedValue() {
        if (this.preProcessedValue == null) {
            if (this.value instanceof String) {
                setPreProcessedValue(getPreProcessFunction().andThen(trim()).andThen(toLowerCase()).apply(this.value.toString()));
            } else {
                setPreProcessedValue(getPreProcessFunction().apply(this.value));
            }
        }
        return this.preProcessedValue;
    }

    public AbstractMap.SimpleEntry getPreprocessedValueWithType() {
        return new AbstractMap.SimpleEntry(this.getElementClassification(), this.getPreProcessedValue());
    }

    public Function<Element, Stream<Token>> getTokenizerFunction() {
        return this.tokenizerFunction;
    }

    public Stream<Token> getTokens() {
        if (this.tokens == null) {
            this.tokens = getTokenizerFunction().apply(this).distinct().collect(Collectors.toList());
        }
        return this.tokens.stream();
    }

    public BiFunction<Token, Token, Double> getSimilarityMatchFunction() {
        return this.similarityMatchFunction;
    }


    /**
     * This gets the Max number of tokens present between matching Elements.
     * For Elements that do not have a balanced set of tokens, it can push the score down.
     * TODO: Need to evaluate an average of the two.
     */
    @Override
    public long getChildCount(Matchable other) {
        if (other instanceof Element) {
            Element o = (Element) other;
            return Math.max(this.getTokens().count(), o.getTokens().count());
        }
        return 0;
    }

    @Override
    public long getUnmatchedChildCount(Matchable other) {
        if (other instanceof Element) {
            Element o = (Element) other;
            long emptyChildren = this.getTokens().filter(token -> token == null || StringUtils.isEmpty(token.getValue().toString())).count();
            long oEmptyChildren = o.getTokens().filter(token -> token == null || StringUtils.isEmpty(token.getValue().toString())).count();
            return Math.max(emptyChildren, oEmptyChildren);
        }
        return 0;
    }

    @Override
    public Function<Match, Score> getScoringFunction() {
        return this.scoringFunction;
    }

    public static class Builder {
        private ElementType type;
        private String variance;
        private Object value;
        private double weight = 1.0;
        private double threshold = 0.3;
        private Function<Object, Object> preProcessFunction;

        private Function<Element, Stream<Token>> tokenizerFunction;
        private BiFunction<Token, Token, Double> similarityMatchFunction;
        private Function<Match, Score> scoringFunction;
        private Function<List<Token>, Stream<Match<Token>>> matchOptimizerFunction;

        public Builder setType(ElementType type) {
            this.type = type;
            return this;
        }

        public Builder setVariance(String variance) {
            this.variance = variance;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setValue(Double value) {
            this.value = value;
            return this;
        }

        public Builder setValue(Date value) {
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

        public Builder setPreProcessingFunction(Function<Object, Object> preProcessingFunction) {
            this.preProcessFunction = preProcessingFunction;
            return this;
        }


        public Builder setTokenizerFunction(Function<Element, Stream<Token>> tokenizerFunction) {
            this.tokenizerFunction = tokenizerFunction;
            return this;
        }

        public Builder setSimilarityMatchFunction(BiFunction<Token, Token, Double> similarityMatchFunction) {
            this.similarityMatchFunction = similarityMatchFunction;
            return this;
        }

        public Builder setScoringFunction(Function<Match, Score> scoringFunction) {
            this.scoringFunction = scoringFunction;
            return this;
        }

        public Builder setMatchOptimizerFunction(Function<List<Token>, Stream<Match<Token>>> matchOptimizerFunction) {
            this.matchOptimizerFunction = matchOptimizerFunction;
            return this;
        }

        public Element createElement() {
            return new Element(type, variance, value, weight, threshold, preProcessFunction, tokenizerFunction,
                    similarityMatchFunction, scoringFunction, matchOptimizerFunction);
        }
    }

    @Override
    public String toString() {
        return "{" +
                "'" + value + '\'' +
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

