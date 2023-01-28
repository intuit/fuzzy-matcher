package com.intuit.fuzzymatcher.domain;

import java.util.List;
import java.util.function.BiFunction;

/**
 *
 * Interface implemented by Document, Element and Token to enable matching and scoring these objects
 */
public interface Matchable {

    public double getWeightedChildCount(Matchable other);

    public BiFunction<Match, List<Score>, Score> getScoringFunction();

    public double getWeight();

    public double getUnmatchedChildWeight(Matchable other);
}
