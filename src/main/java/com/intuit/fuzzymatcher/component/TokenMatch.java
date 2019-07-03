package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.*;
import com.intuit.fuzzymatcher.function.SimilarityMatchFunction;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Matches at Token level, this class uses the SimilarityMatchFunction to get a score at a Token level
 * This class also optimizes which tokens undergo match, by breaking it to NGram and figuring out the Search Groups
 */
public class TokenMatch {

    public Stream<Match<Token>> matchTokens(Stream<Token> input) {
        Map<ElementClassification, List<Token>> tokenClassMap = input.collect(Collectors
                .groupingBy(token -> token.getElement().getElementClassification()));
        return tokenClassMap.entrySet().parallelStream().
                flatMap(entry -> entry.getKey().getMatchOptimizerFunction().apply(entry.getValue()))
                .filter(tokenMatch -> tokenMatch.getResult() >= tokenMatch.getData().getElement().getThreshold());
    }
}
