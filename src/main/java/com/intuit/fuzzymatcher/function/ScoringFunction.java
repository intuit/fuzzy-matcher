package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.Matchable;
import com.intuit.fuzzymatcher.domain.Score;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A functional interface to get a score between 2 Match objects
 */
public interface ScoringFunction extends BiFunction<Match, List<Score>, Score> {

    double EXPONENT = 1.5;
    double EXPONENTIAL_INCREASE_THRESHOLD = 0.9;
    double DEFAULT_UNMATCHED_CHILD_SCORE = 0.5;

    /**
     * For all the childScores in a Match object, it calculates the weighted average.
     * To get a balanced average for 2 Match object which do not have same number of childScores.
     * It gives a score of 0.5 for missing children
     *
     * Follows the same rules as "getAverageScore" and in addition applies weights to children.
     * It can be used for aggregating Elements to Documents, where weights can be provided at Element level
     *
     * @return the scoring function for WeightedAverage
     */
    static ScoringFunction getWeightedAverageScore() {
        return (match, childScores) -> {
            double numerator = getSumOfWeightedResult(childScores)
                    + getUnmatchedChildScore(match);
            double denominator = getWeightedChildCount(match);
            return new Score(numerator / denominator, match);
        };
    }

    /**
     * This is the default scoring used to calculate the Document score by aggregating the child Element scores.
     * This combines the benefits of weights and exponential increase when calculating the average scores.
     *
     * @return the scoring function for ExponentialWeightedAverage
     */
    static ScoringFunction getExponentialWeightedAverageScore() {
        return (match, childScores) -> {
            List<Score> perfectMatchedElements = getPerfectMatchedElement(childScores);

            // Apply Exponent if match elements > 1
            if (perfectMatchedElements.size() > 1 && getSumOfWeightedResult(perfectMatchedElements) > 1) {
                List<Score> notPerfectMachedElements = getNonPerfectMatchedElement(childScores);
                double numerator = getExponentiallyIncreasedValue(getSumOfWeightedResult(perfectMatchedElements))
                        + getSumOfWeightedResult(notPerfectMachedElements)
                        + getUnmatchedChildScore(match);

                double denominator = getExponentiallyIncreasedValue(getSumOfWeights(perfectMatchedElements))
                        + getSumOfWeights(notPerfectMachedElements)
                        + getUnmatchedChildWeight(match);
                return new Score(numerator / denominator, match);
            } else
                return getWeightedAverageScore().apply(match, childScores);
        };
    }

    static double getSumOfWeightedResult(List<Score> childScoreList) {
        return (childScoreList.stream().mapToDouble(d -> d.getResult() * d.getMatch().getWeight())).sum();
    }

    static double getSumOfWeights(List<Score> childScoreList) {
        return (childScoreList.stream().mapToDouble(d -> d.getMatch().getWeight())).sum();
    }

    static double getExponentiallyIncreasedValue(double value) {
        return Math.pow(value, EXPONENT);
    }

    static List<Score> getNonPerfectMatchedElement(List<Score> childScoreList) {
        return childScoreList.stream()
                .filter(d -> d.getResult() < EXPONENTIAL_INCREASE_THRESHOLD)
                .collect(Collectors.toList());
    }

    static List<Score> getPerfectMatchedElement(List<Score> childScoreList) {
        return childScoreList.stream()
                .filter(d -> d.getResult() >= EXPONENTIAL_INCREASE_THRESHOLD)
                .collect(Collectors.toList());
    }

    static double getWeightedChildCount(Match match) {
        return match.getData().getWeightedChildCount(match.getMatchedWith());
    }

    static double getUnmatchedChildWeight(Match match) {
        return match.getData().getUnmatchedChildWeight(match.getMatchedWith());
    }

    static double getUnmatchedChildScore(Match match) {
        return DEFAULT_UNMATCHED_CHILD_SCORE * getUnmatchedChildWeight(match);
    }


}
