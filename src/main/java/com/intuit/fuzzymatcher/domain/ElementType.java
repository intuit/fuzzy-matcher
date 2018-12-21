package com.intuit.fuzzymatcher.domain;


import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.function.PreProcessFunction.*;
import static com.intuit.fuzzymatcher.function.TokenizerFunction.*;
import static com.intuit.fuzzymatcher.function.SimilarityMatchFunction.*;

/**
 *
 * Enum to define different types of Element.
 * This is used only to categorize the data, and apply functions at different stages of match.
 * The functions, can be overridden from Element class using the appropriate setters at the time of creation.
 */
public enum ElementType {
    NAME(namePreprocessing(), wordTokenizer(), soundex()),
    TEXT(removeSpecialChars(), wordTokenizer(), soundex()),
    ADDRESS(addressPreprocessing(), wordTokenizer(), soundex()),
    EMAIL(removeDomain(), triGramTokenizer(),  equality()),
    PHONE(usPhoneNormalization(),decaGramTokenizer(), equality());

    private final Function<String, String> preProcessFunction;

    private final Function<Element, Stream<Token>> tokenizerFunction;

    private final BiFunction<Token, Token, Double> similarityMatchFunction;

    ElementType(Function<String, String> preProcessFunction, Function<Element, Stream<Token>> tokenizerFunction,
                BiFunction<Token, Token, Double> similarityMatchFunction) {
        this.preProcessFunction = preProcessFunction;
        this.tokenizerFunction = tokenizerFunction;
        this.similarityMatchFunction = similarityMatchFunction;
    }

    public Function<String, String> getPreProcessFunction() {
        return preProcessFunction;
    }


    public Function<Element, Stream<Token>> getTokenizerFunction() {
        return tokenizerFunction;
    }

    public BiFunction<Token, Token, Double> getSimilarityMatchFunction() {
        return similarityMatchFunction;
    }

}
