package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Matches at Token level, this class uses the SimilarityMatchFunction to get a score at a Token level
 * This class also optimizes which tokens undergo match, by breaking it to NGram and figuring out the Search Groups
 */
public class TokenMatch {

    public Stream<Match<Token>> matchTokens(Stream<Token> input) {
        List<Token> tokenList = input.collect(Collectors.toList());
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

    private void initializeSearchGroups(List<Token> input) {

        getGroupsByNGramAndType(input).forEach(groups -> {

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

    private Stream<List<Token>> getGroupsByNGramAndType(List<Token> input) {
        Stream<NGram> nGramStream = input.parallelStream().flatMap(token -> token.getNGrams());

        Map<String, Map<String, List<Token>>> elementTypeByNgramByTokenMap =  nGramStream
                .collect(Collectors.groupingBy(nGram -> nGram.getToken().getElement().getClassification(),
                        Collectors.groupingBy(NGram::getValue,
                                Collectors.mapping(NGram::getToken, Collectors.toList()))));

        return elementTypeByNgramByTokenMap.values().stream().flatMap(map -> map.values().stream());
    }
}
