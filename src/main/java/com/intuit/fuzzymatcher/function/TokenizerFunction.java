package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Token;
import com.intuit.fuzzymatcher.util.Utils;
import org.apache.commons.codec.language.Soundex;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A functional interface to Tokenize Elements
 */
public interface TokenizerFunction extends Function<Element, Stream<Token>> {

    final Soundex soundex = new Soundex();

    static TokenizerFunction valueTokenizer() {
        return (element -> Stream.of(getToken(element, element.getPreProcessedValue(), false)));
    }

    static TokenizerFunction wordTokenizer() {
        return (element) -> Arrays.stream(element.getPreProcessedValue().toString().split("\\s+"))
                .map(token -> getToken(element, token, false));
    }

    static TokenizerFunction wordSoundexEncodeTokenizer() {
        return (element) -> Arrays.stream(element.getPreProcessedValue().toString().split("\\s+"))
                .map(val -> {
                    String code = val;
                    if (!Utils.isNumeric(val)) {

                        code = soundex.encode(val);
                        if (code.equals("")) {
                            code = val;
                        }
                    }
                    // System.out.println(val +"->" + code);
                    return code;
                }).map(token -> getToken(element, token, false));
    }

    static TokenizerFunction triGramTokenizer() {
        return (element) -> getTokens(3, element);
    }

    static TokenizerFunction decaGramTokenizer() {
        return (element) -> getTokens(10, element);
    }


    static Stream<Token> getTokens(int size, Element element) {
        Object elementValue = element.getPreProcessedValue();
        return Utils.getNGrams(elementValue, size)
                .map(str -> getToken(element, str, true));

    }

    static Token getToken(Element element, Object token, boolean nGramTokenized) {
        return new Token(token, element, nGramTokenized);
    }
}
