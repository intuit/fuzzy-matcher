package com.intuit.fuzzymatcher.domain;

/**
 * This holds the result of matching 2 Documents, Elements or Tokens.
 * This also holds the reference of the Match object, used to aggregate score with the ScoringFunction.
 */
public class Score {
    private double result;

    private Match match;

    public Score(double result, Match match) {
        this.result = result;
        this.match = match;
    }

    public double getResult() {
        return result;
    }


    public Match getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return "Score{" +
                "result=" + result +
                ", match=" + match +
                '}';
    }
}
