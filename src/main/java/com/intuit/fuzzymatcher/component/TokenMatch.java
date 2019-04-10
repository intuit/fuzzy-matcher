package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
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
        Map<String, List<Token>> tokenClassMap =  input.collect(Collectors
                .groupingBy(token -> token.getElement().getClassification()));
        return tokenClassMap.entrySet().parallelStream().flatMap(entry -> {
            if (entry.getKey().startsWith(ElementType.NUMBER.toString())) {
                return matchTokensWithSortOptimization(entry.getValue());
            }
            return matchTokensWithSearchGroupsOptimization(entry.getValue());
        });
    }

    public Stream<Match<Token>> matchTokensWithSearchGroupsOptimization(List<Token> tokenList) {
        initializeSearchGroups(tokenList);

        return tokenList.parallelStream()
                .filter(left -> BooleanUtils.isNotFalse(left.getElement().getDocument().isSource()))
                .flatMap(
                        left -> left.getSearchGroups()
                                .filter(right -> right != null && !left.getElement().getDocument().getKey().equals(right.getElement().getDocument().getKey()))
                                .map(right -> {
                                    double result = left.getElement().getSimilarityMatchFunction().apply(left, right);
                                    return new Match<Token>(left, right, result);
                                })
                                .filter(match -> match.getResult() >= match.getData().getElement().getThreshold())
                );
    }



    public Stream<Match<Token>> matchTokensWithSortOptimization(List<Token> tokenList) {
        Collections.sort(tokenList);
        List<Match<Token>> matchList = new ArrayList<>();
        for (int i= 0; i < tokenList.size(); i++) {
            Token left =  tokenList.get(i);
            for (int j = i +1 ; j < tokenList.size(); j++) {
                Token right = tokenList.get(j);
                if (!left.getElement().getDocument().getKey().equals(right.getElement().getDocument().getKey())) {
                    double result = left.getElement().getSimilarityMatchFunction().apply(left, right);
//                    if(result < left.getElement().getThreshold()) {
//                        break;
//                    }
                    matchList.add(new Match<Token>(left, right, result));
                }
            }
        }
        return matchList.stream();
    }

    private void initializeSearchGroups(List<Token> input) {

        getGroupsByNGram(input).forEach(groups -> {

            groups.stream()
                    .filter(t -> BooleanUtils.isNotFalse(t.getElement().getDocument().isSource()))
                    .distinct()
                    .forEach(left -> {
                        List<Token> matchGroups = groups.stream()
                                .filter(right -> !left.getElement().getDocument().isMatchedWith(right.getElement().getDocument()))
                                .collect(Collectors.toList());

                        // Add Search Groups to reduce complexity
                        left.setSearchGroups(Stream.concat(matchGroups.stream(), left.getSearchGroups()));

                        // Set Matched With Docs to reduce complexity to LogN
                        matchGroups.parallelStream().forEach(right -> {
                            right.getElement().getDocument().addMatchedWith(left.getElement().getDocument());
                        });
                    });
        });

    }

    private Stream<List<Token>> getGroupsByNGram(List<Token> input) {
        Stream<NGram> nGramStream = input.parallelStream().flatMap(token -> token.getNGrams());

        Map<String, List<Token>> ngramByTokenMap =  nGramStream
                .collect(Collectors.groupingBy(NGram::getValue,
                                Collectors.mapping(NGram::getToken, Collectors.toList())));

        return ngramByTokenMap.values().stream();
    }
}
