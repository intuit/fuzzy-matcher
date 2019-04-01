package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Matches at Token level, this class uses the SimilarityMatchFunction to get a score at a Token level
 * This class also optimizes which tokens undergo match, by breaking it to NGram and figuring out the Search Groups
 */
public class TokenMatch {

    public Stream<Match<Token>> matchTokens(Stream<Token> input) {
        List<Token> tokenList = input.collect(Collectors.toList());
        initializeSearchGroups(tokenList);

//        AtomicInteger index = new AtomicInteger();
//        List<Match<Token>> res =  tokenList.parallelStream()
        return tokenList.parallelStream()
                .filter(left -> BooleanUtils.isNotFalse(left.getElement().getDocument().isSource()))
                .flatMap(
                        left -> left.getSearchGroups()
                                .filter(right -> right != null && !left.getElement().getDocument().getKey().equals(right.getElement().getDocument().getKey()))
                                //.filter(right -> !right.containstMatchedTokens(left))
                                .map(right -> {
                                    //index.incrementAndGet();
                                    double result = left.getElement().getSimilarityMatchFunction().apply(left, right);
//                                    Match<Token> leftMatch =  new Match<Token>(left, right, result);
//                                    if (BooleanUtils.isNotFalse(right.getElement().getDocument().isSource())) {
//                                        return Stream.of(leftMatch, new Match<Token>(right, left, result));
//                                    } else {
//                                        return Stream.of(leftMatch);
//                                    }
                                    return new Match<Token>(left, right, result);
                                })
                                .filter(match -> match.getResult() >= match.getData().getElement().getThreshold())
                );
//                .collect(Collectors.toList());
//        System.out.println("conter : " + index.intValue());
//        return  res.stream();
    }

    private void initializeSearchGroups(List<Token> input) {
        Stream<NGram> nGramStream = input.parallelStream().flatMap(token -> token.getNGrams());
        Map<ElementType, List<NGram>> elementTypeNGramMap = nGramStream
                .collect(Collectors.groupingBy(ngram -> ngram.getToken().getElement().getType()));

        elementTypeNGramMap.entrySet().parallelStream().forEach(entry -> {
            List<NGram> ngramsList = entry.getValue();
            Map<String, List<NGram>> stringNGramMap = ngramsList.parallelStream().collect(Collectors.groupingBy(NGram::getValue));

            stringNGramMap.entrySet().stream().forEach(stringListEntry -> {
                List<Token> groups = stringListEntry.getValue().parallelStream()
                        .map(NGram::getToken)
                        .distinct()
                        .collect(Collectors.toList());

                groups.forEach(left -> {
                    List<Token> matchGroups = groups.stream()
                            .filter(right -> !left.getElement().getDocument().isMatchedWith(right.getElement().getDocument()))
                            .collect(Collectors.toList());

                    // Add Search Groups to reduce complexity
                    if (BooleanUtils.isNotFalse(left.getElement().getDocument().isSource())) {
                        left.setSearchGroups(Stream.concat(matchGroups.stream(), left.getSearchGroups()));
                    }

                    // Set Matched With Docs to reduce complexity to LogN
                    matchGroups.parallelStream().forEach(right -> {
                        right.getElement().getDocument().addMatchedWith(left.getElement().getDocument());
                    });
                });

            });
        });
    }
}
