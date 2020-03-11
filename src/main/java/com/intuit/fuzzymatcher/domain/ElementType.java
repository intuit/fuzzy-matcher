package com.intuit.fuzzymatcher.domain;


import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.MatchType.*;
import static com.intuit.fuzzymatcher.function.MatchOptimizerFunction.*;
import static com.intuit.fuzzymatcher.function.PreProcessFunction.*;
import static com.intuit.fuzzymatcher.function.SimilarityMatchFunction.*;
import static com.intuit.fuzzymatcher.function.TokenizerFunction.*;
/**
 * Enum to define different types of Element.
 * This is used only to categorize the data, and apply functions at different stages of match.
 * The functions, can be overridden from Element class using the appropriate setters at the time of creation.
 */
public enum ElementType {
    NAME(namePreprocessing(), wordSoundexEncodeTokenizer(), soundex(), searchGroupOptimizer(), EQUALITY),
    TEXT(removeSpecialChars(), wordSoundexEncodeTokenizer(), soundex(), searchGroupOptimizer(), EQUALITY),
    ADDRESS(addressPreprocessing(), wordSoundexEncodeTokenizer(), soundex(), searchGroupOptimizer(), EQUALITY),
    EMAIL(removeDomain(), triGramTokenizer(), equality(), searchGroupOptimizer(), EQUALITY),
    PHONE(usPhoneNormalization(), decaGramTokenizer(), equality(), searchGroupOptimizer(), EQUALITY),
    NUMBER(numberPreprocessing(), valueTokenizer(), numberDifferenceRate(), numberSortOptimizer(), NEAREST_NEIGHBOURS),
    DATE(none(), valueTokenizer(), dateDifferenceWithinYear(), dateSortOptimizer(), NEAREST_NEIGHBOURS);


    private final Function<Object, Object> preProcessFunction;

    private final Function<Element, Stream<Token>> tokenizerFunction;

    private final BiFunction<Token, Token, Double> similarityMatchFunction;

    private final Function<List<Token>, Stream<Match<Token>>> matchOptimizerFunction;

    private final MatchType matchType;

    ElementType(Function<Object, Object> preProcessFunction, Function<Element, Stream<Token>> tokenizerFunction,
                BiFunction<Token, Token, Double> similarityMatchFunction, Function<List<Token>, Stream<Match<Token>>> matchOptimizerFunction,
                MatchType matchType) {
        this.preProcessFunction = preProcessFunction;
        this.tokenizerFunction = tokenizerFunction;
        this.similarityMatchFunction = similarityMatchFunction;
        this.matchOptimizerFunction = matchOptimizerFunction;
        this.matchType = matchType;
    }

    public Function<Object, Object> getPreProcessFunction() {
        return preProcessFunction;
    }


    public Function<Element, Stream<Token>> getTokenizerFunction() {
        return tokenizerFunction;
    }

    public BiFunction<Token, Token, Double> getSimilarityMatchFunction() {
        return similarityMatchFunction;
    }

    public Function<List<Token>, Stream<Match<Token>>> getMatchOptimizerFunction() {
        return matchOptimizerFunction;
    }

    public MatchType getMatchType() {
        return this.matchType;
    }
}
