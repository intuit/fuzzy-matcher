package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementClassification;
import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.MatchType;
import com.intuit.fuzzymatcher.domain.Token;
import com.intuit.fuzzymatcher.exception.MatchException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TokenRepo {

    private Map<ElementClassification, Repo> repoMap;

    public TokenRepo() {
        this.repoMap = new ConcurrentHashMap<>();
    }

    public void put(Token token) {

        ElementClassification elementClassification = token.getElement().getElementClassification();
        Repo repo = repoMap.get(elementClassification);

        if (repo == null) {
            repo = new Repo(token.getElement().getMatchType());
            repoMap.put(elementClassification, repo);
        }
        repo.put(token, token.getElement());
    }

    public Set<Element> get(Token token) {
        Repo repo = repoMap.get(token.getElement().getElementClassification());
        if (repo != null) {
            return repo.get(token);
        }
        return null;
    }

    private class Repo {

        MatchType matchType;

        Map<Object, Set<Element>> tokenElementSet;

        TreeSet<Object> tokenBinaryTree;

        private static final double MAX_AGE_DIFF = 5;
        private static final double MIN_AGE_DIFF = 1;

        Repo(MatchType matchType) {
            this.matchType = matchType;
            switch (matchType) {
                case NEAREST_NEIGHBORS:
                    tokenBinaryTree = new TreeSet<>();
                case EQUALITY:
                    tokenElementSet = new ConcurrentHashMap<>();
            }
        }

        void put(Token token, Element element) {
            switch (matchType) {
                case NEAREST_NEIGHBORS:
                    tokenBinaryTree.add(token.getValue());
                case EQUALITY:
                    Set<Element> elements = tokenElementSet.getOrDefault(token.getValue(), new HashSet<>());
                    elements.add(element);
                    tokenElementSet.put(token.getValue(), elements);
            }
        }

        Set<Element> get(Token token) {
            switch (matchType) {
                case EQUALITY:
                    return tokenElementSet.get(token.getValue());
                case NEAREST_NEIGHBORS:
                    TokenRange tokenRange;
                    if (token.getElement().getElementClassification().getElementType().equals(ElementType.AGE)) {
                        tokenRange = new TokenRange(token, token.getElement().getNeighborhoodRange(), MAX_AGE_DIFF, MIN_AGE_DIFF);
                    } else {
                        tokenRange = new TokenRange(token, token.getElement().getNeighborhoodRange());
                    }
                    return tokenBinaryTree.subSet(tokenRange.lower, true, tokenRange.higher, true)
                            .stream()
                            .flatMap(val -> tokenElementSet.get(val).stream()).collect(Collectors.toSet());

            }
            return null;
        }
    }

    private class TokenRange {

        private final Object lower;
        private final Object higher;
        private static final double DATE_SCALE_FACTOR = 1.1;

        TokenRange(Token token, double pct, double maxDiff, double minDiff) {
            Object value = token.getValue();
            double absMaxDiff = Math.abs(maxDiff);
            double absMinDiff = Math.abs(minDiff);
            if (value instanceof Double) {
                this.lower = getLower((Double) value, pct, absMaxDiff, absMinDiff).doubleValue();
                this.higher = getHigher((Double) value, pct, absMaxDiff, absMinDiff).doubleValue();
            } else if (value instanceof Integer) {
                this.lower = getLower((Integer) value, pct, absMaxDiff, absMinDiff).intValue();
                this.higher = getHigher((Integer) value, pct, absMaxDiff, absMinDiff).intValue();
            } else if (value instanceof Long) {
                this.lower = getLower((Long) value, pct, absMaxDiff, absMinDiff).longValue();
                this.higher = getHigher((Long) value, pct, absMaxDiff, absMinDiff).longValue();
            } else if (value instanceof Float) {
                this.lower = getLower((Float) value, pct, absMaxDiff, absMinDiff).floatValue();
                this.higher = getHigher((Float) value, pct, absMaxDiff, absMinDiff).floatValue();
            } else if (value instanceof Date) {
                this.lower = new Date(getLower(((Date) value).getTime(), pct * DATE_SCALE_FACTOR, absMaxDiff, absMinDiff).longValue());
                this.higher = new Date(getHigher(((Date) value).getTime(), pct * DATE_SCALE_FACTOR, absMaxDiff, absMinDiff).longValue());
            } else {
                throw new MatchException("Data Type not supported");
            }
        }

        TokenRange(Token token, double pct) {
            this(token, pct, Double.MAX_VALUE, 0);
        }

        private Number getLower(Number number, double pct, double maxDiff, double minDiff) {
            Double dnum = number.doubleValue();
            Double pctVal = Math.abs(dnum * (1.0 - pct));
            Double diff = Math.min(maxDiff, Math.max(minDiff, pctVal));
            return dnum - diff;
        }

        private Number getHigher(Number number, double pct, double maxDiff, double minDiff) {
            Double dnum = number.doubleValue();
            Double pctVal = Math.abs(dnum * (1.0 - pct));
            Double diff = Math.min(maxDiff, Math.max(minDiff, pctVal));
            return dnum + diff;
        }

    }

}
