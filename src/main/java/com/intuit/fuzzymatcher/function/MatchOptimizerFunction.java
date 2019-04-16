package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MatchOptimizerFunction extends Function<List<Token>, Stream<Match<Token>>> {

    static MatchOptimizerFunction numberSortOptimizer() {
        return (tokenList) -> {
            Collections.sort(tokenList, new Comparator<Token>() {
                @Override
                public int compare(Token o1, Token o2) {
                    return stringToDouble(o1.getValue().toString()).compareTo(stringToDouble(o2.getValue().toString()));
                }
            });
            return applySortMatch(tokenList);
        };
    }

    static Double stringToDouble(String str) {
        try {
            return Double.valueOf(str);
        } catch (NumberFormatException nfe) {
            return Double.NaN;
        }
    }

    static MatchOptimizerFunction textSortOptimizer() {
        return (tokenList) -> {
            Collections.sort(tokenList, new Comparator<Token>() {
                @Override
                public int compare(Token o1, Token o2) {
                    return o1.getValue().toString().compareTo(o2.getValue().toString());
                }
            });
            return applySortMatch(tokenList);
        };
    }

    static Stream<Match<Token>> applySortMatch(List<Token> tokenList) {
        List<Match<Token>> matchList = new ArrayList<>();
        for (int i = 0; i < tokenList.size(); i++) {
            Token left = tokenList.get(i);
            for (int j = i + 1; j < tokenList.size(); j++) {
                Token right = tokenList.get(j);
                if (!left.getElement().getDocument().getKey().equals(right.getElement().getDocument().getKey())) {
                    double result = left.getElement().getSimilarityMatchFunction().apply(left, right);
                    // TODO: Should break at a Token Threshold (which should be a function of Element Threshold)
                    if(result < left.getElement().getThreshold()) {
                        break;
                    }
                    matchList.add(new Match<Token>(left, right, result));
                }
            }
        }
        return matchList.stream();
    }

    static MatchOptimizerFunction searchGroupOptimizer() {
        return (tokenList) -> {
            //List<Token> tokenList = tokenStream.collect(Collectors.toList());
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
        };
    }

    static void initializeSearchGroups(List<Token> input) {

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

    static Stream<List<Token>> getGroupsByNGram(List<Token> input) {
        Stream<NGram> nGramStream = input.parallelStream().flatMap(token -> token.getNGrams());

        Map<Object, List<Token>> ngramByTokenMap = nGramStream
                .collect(Collectors.groupingBy(NGram::getValue,
                        Collectors.mapping(NGram::getToken, Collectors.toList())));

        return ngramByTokenMap.values().stream().distinct().filter(list -> list.size() > 1);
    }

}
