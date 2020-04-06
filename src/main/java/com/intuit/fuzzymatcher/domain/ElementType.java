package com.intuit.fuzzymatcher.domain;


import java.util.function.Function;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.MatchType.EQUALITY;
import static com.intuit.fuzzymatcher.domain.MatchType.NEAREST_NEIGHBOURS;
import static com.intuit.fuzzymatcher.function.PreProcessFunction.*;
import static com.intuit.fuzzymatcher.function.TokenizerFunction.*;

/**
 * Enum to define different types of Element.
 * This is used only to categorize the data, and apply functions at different stages of match.
 * The functions, can be overridden from Element class using the appropriate setters at the time of creation.
 */
public enum ElementType {
    NAME,
    TEXT,
    ADDRESS,
    EMAIL,
    PHONE,
    NUMBER,
    DATE;

    protected Function<Object, Object> getPreProcessFunction() {
        switch (this) {
            case NAME:
                return namePreprocessing();
            case TEXT:
                return removeSpecialChars();
            case ADDRESS:
                return addressPreprocessing();
            case EMAIL:
                return removeDomain();
            case PHONE:
                return usPhoneNormalization();
            case NUMBER:
                return numberPreprocessing();
            case DATE:
                return none();
            default:
                return none();
        }
    }

    protected Function<Element, Stream<Token>> getTokenizerFunction() {
        switch (this) {
            case NAME:
                return wordSoundexEncodeTokenizer();
            case TEXT:
                return wordTokenizer();
            case ADDRESS:
                return wordSoundexEncodeTokenizer();
            case EMAIL:
                return triGramTokenizer();
            case PHONE:
                return decaGramTokenizer();
            case NUMBER:
                return valueTokenizer();
            case DATE:
                return valueTokenizer();
            default:
                return valueTokenizer();
        }
    }

    protected MatchType getMatchType() {
        switch (this) {
            case NUMBER:
                return NEAREST_NEIGHBOURS;
            case DATE:
                return NEAREST_NEIGHBOURS;
            default:
                return EQUALITY;
        }
    }
}