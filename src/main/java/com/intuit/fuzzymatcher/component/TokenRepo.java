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

        private final Double AGE_PCT_OF = 10D;
        private final Double DATE_PCT_OF = 15777e7D; // 5 years of range


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
                    switch (token.getElement().getElementClassification().getElementType()){
                        case AGE:
                            tokenRange = new TokenRange(token, token.getElement().getNeighborhoodRange(), AGE_PCT_OF);
                            break;
                        case DATE:
                            tokenRange = new TokenRange(token, token.getElement().getNeighborhoodRange(), DATE_PCT_OF);
                            break;
                        default:
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

        TokenRange(Token token, double pct, Double pctOf) {
            Object value = token.getValue();
            if (value instanceof Double) {
                this.lower = getLower((Double) value, pct, pctOf).doubleValue();
                this.higher = getHigher((Double) value, pct, pctOf).doubleValue();
            } else if (value instanceof Integer) {
                this.lower = getLower((Integer) value, pct, pctOf).intValue();
                this.higher = getHigher((Integer) value, pct, pctOf).intValue();
            } else if (value instanceof Long) {
                this.lower = getLower((Long) value, pct, pctOf).longValue();
                this.higher = getHigher((Long) value, pct, pctOf).longValue();
            } else if (value instanceof Float) {
                this.lower = getLower((Float) value, pct, pctOf).floatValue();
                this.higher = getHigher((Float) value, pct, pctOf).floatValue();
            } else if (value instanceof Date) {
                this.lower = new Date(getLower(((Date) value).getTime(), pct, pctOf).longValue());
                this.higher = new Date(getHigher(((Date) value).getTime(), pct, pctOf).longValue());
            } else {
                throw new MatchException("Data Type not supported");
            }
        }

        TokenRange(Token token, double pct) {
            this(token, pct, null);
        }

        private Number getLower(Number number, double pct, Double pctOf) {
            Double dnum = number.doubleValue();
            Double dPctOf = pctOf != null ? pctOf : dnum;
            Double pctVal = Math.abs(dPctOf * (1.0 - pct));
            return dnum - pctVal;
        }

        private Number getHigher(Number number, double pct, Double pctOf) {
            Double dnum = number.doubleValue();
            Double dPctOf = pctOf != null ? pctOf : dnum;
            Double pctVal = Math.abs(dPctOf * (1.0 - pct));
            return dnum + pctVal;
        }

    }

}
