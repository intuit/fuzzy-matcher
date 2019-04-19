package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
import com.intuit.fuzzymatcher.exception.MatchException;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * These functions are used to improve performance and reduce complexity before the match is applied.
 * Without an optimizer, all the Tokens are matched against every other, and give an O(N^2) complexity.
 * These functions attempt to eliminate Token's which have a very low probability to match.
 */
public interface MatchOptimizerFunction extends Function<List<Token>, Stream<Match<Token>>> {

    /**
     * Sorts the Token list with their values. Converts all values to a Double to allow a numeric sort.
     * After the sort, each Token are matched only with their nearest neighbours.
     * The matches are performed until a match result falls below a Threshold defined (in Element)
     *
     * eg: input [80, 2, 90, 3, 100] - sorted [2, 3, 80, 90, 100] - 2 is matched with 3 and 80 only.
     * Since 2 and 80 match will fall below Threshold, further matches are not attempted
     *
     * @return MatchOptimizerFunction
     */
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

    /**
     * Utility to convert String to Date
     * @param str
     * @return
     */
    static Double stringToDouble(String str) {
        try {
            return Double.valueOf(str);
        } catch (NumberFormatException nfe) {
            return Double.NaN;
        }
    }

    /**
     * Sorts the Token list with their values. Converts all values to a String to allow a string sort.
     * After the sort, each Token are matched only with their nearest neighbours.
     * The matches are performed until a match result falls below a Threshold defined (in Element)
     *
     * eg: input ["a", "z", "k", "a", l] - sorted ["a", "a", "k", "l", "z"] - a is matched with a and k only.
     * Since a and k match will fall below Threshold, further matches are not attempted
     *
     * Note: for Strings, this is relevant only if we are looking for matches that are equal.
     * If using Soundex or any other matching function, "searchGroupOptimizer" should be preferred
     *
     * @return MatchOptimizerFunction
     */
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

    /**
     * Sorts the Token list with their values. Converts all values to a Date to allow a date sort.
     * (Throws an exception is value not of Date type)
     * After the sort, each Token are matched only with their nearest neighbours.
     * The matches are performed until a match result falls below a Threshold defined (in Element)
     *
     * eg: input [Jan-1-2020, Dec-1-2020, Nov-1-2020, Jan-2-2020] - sorted [Jan-1-2020, Jan-2-2020, Nov-1-2020, Dec-1-2020]
     * Jan-1-2020 is matched with Jan-2-2020 and Nov-1-2020 only.
     * Since Jan-1-2020 and Nov-1-2020 match will fall below Threshold, further matches are not attempted
     *
     * @return MatchOptimizerFunction
     */
    static MatchOptimizerFunction dateSortOptimizer() {
        return (tokenList) -> {
            Collections.sort(tokenList, new Comparator<Token>() {
                @Override
                public int compare(Token o1, Token o2) {
                    if (!(o1.getValue() instanceof Date && o2.getValue() instanceof Date)) {
                        throw new MatchException("input values are not Dates");
                    }
                    return ((Date)o1.getValue()).compareTo((Date)o2.getValue());
                }
            });
            return applySortMatch(tokenList);
        };
    }

    /**
     * Does the matching for all Sort Optimizers.
     *
     * @param tokenList
     * @return Stream<Match<Token>>
     */
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

    /**
     * Reduces the complexity by not performing matches against the elements which have a very low probability to match by creating "Search Groups".
     *
     * Take an example of list of names to match ["steve","parker","stephen"]
     * These names are broken down into tri-grams like this
     * steve - [ste,tev,eve]
     * parker - [par,ark,rke,ker]
     * stephen - [ste,tep,eph,phe,hen]
     *
     * Here only the 1st and 3rd names have tri-grams "ste" in common (and a search group is created for them.)
     * The match algorithm assumes a very low probability that "parker" will match with the other 2, and hence no match is attempted with it
     * @return MatchOptimizerFunction
     */
    static MatchOptimizerFunction searchGroupOptimizer() {
        return (tokenList) -> {
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
