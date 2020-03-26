package com.intuit.fuzzymatcher.domain;


import com.intuit.fuzzymatcher.function.PreProcessFunction;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.MatchType.EQUALITY;
import static com.intuit.fuzzymatcher.domain.MatchType.NEAREST_NEIGHBORS;
import static com.intuit.fuzzymatcher.function.PreProcessFunction.*;
import static com.intuit.fuzzymatcher.function.TokenizerFunction.*;
/**
 * Enum to define different types of Element.
 * This is used only to categorize the data, and apply functions at different stages of match.
 * The functions, can be overridden from Element class using the appropriate setters at the time of creation.
 */
public enum ElementType {
    NAME(namePreprocessing(), wordSoundexEncodeTokenizer(), EQUALITY),
    TEXT(removeSpecialChars(), wordTokenizer(), EQUALITY),
    ADDRESS(addressPreprocessing(), wordSoundexEncodeTokenizer(), EQUALITY),
    EMAIL(removeDomain(), triGramTokenizer(), EQUALITY),
    PHONE(usPhoneNormalization(), decaGramTokenizer(), EQUALITY),
    NUMBER(numberPreprocessing(), valueTokenizer(), NEAREST_NEIGHBORS),
    DATE(none(), valueTokenizer(), NEAREST_NEIGHBORS);


    private final Function preProcessFunction;

    private final Function<Element, Stream<Token>> tokenizerFunction;

    private final MatchType matchType;

    ElementType(Function preProcessFunction, Function<Element, Stream<Token>> tokenizerFunction,
                MatchType matchType) {
        this.preProcessFunction = preProcessFunction;
        this.tokenizerFunction = tokenizerFunction;
        this.matchType = matchType;
    }

    protected Function getPreProcessFunction() {
        return preProcessFunction;
    }

    protected Function<Element, Stream<Token>> getTokenizerFunction() {
        return tokenizerFunction;
    }

    protected MatchType getMatchType() {
        return this.matchType;
    }
}
