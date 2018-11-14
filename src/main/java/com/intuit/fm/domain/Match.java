package com.intuit.fm.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * A generic class to hold the match between 2 objects and the score of the match result.
 * A match between similar Token, Element or Document is represented by this class.
 * <p>
 * The "data" and "matchedWith" object holds the 2 records that matched. And "score" represents the match for these 2 objects.
 * "childScore" is used by ScoringFunction to aggregate and calculate the "score" value
 */
public class Match<T extends Matchable> {

    public Match(T t, T matchedWith, List<Score> childScores) {
        this.data = t;
        this.matchedWith = matchedWith;
        this.childScores = childScores;
    }

    public Match(T t, T matchedWith, double result) {
        this.data = t;
        this.matchedWith = matchedWith;
        this.score = new Score(result, this);
    }

    private T data;

    private T matchedWith;

    private Score score;

    private List<Score> childScores;

    public T getData() {
        return this.data;
    }

    public T getMatchedWith() {
        return matchedWith;
    }

    public void setMatchedWith(T matchedWith) {
        this.matchedWith = matchedWith;
    }

    public double getResult() {
        return this.getScore().getResult();
    }

    public Score getScore() {
        if (this.score == null) {
            this.score = this.data.getScoringFunction().apply(this);
        }
        return this.score;
    }

    public List<Score> getChildScores() {
        return getMaxDistinctScores(this.childScores);
    }

    private List<Score> getMaxDistinctScores(List<Score> scoreList) {
        Map<Matchable, Optional<Score>> map = scoreList.stream()
                .collect(Collectors.groupingBy(s -> s.getMatch().getData(),
                        Collectors.maxBy(Comparator.comparingDouble(Score::getResult))));

        return map.entrySet().stream().map(entry -> entry.getValue().get()).collect(Collectors.toList());
    }

    public double getWeight() {
        return getData().getWeight();
    }

    @Override
    public String toString() {
        return "{" +
                data +
                ", " + score.getResult() +
                '}';
    }
}
