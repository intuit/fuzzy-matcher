package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TokenRepo {

    private Map<ElementClassification, Repo> repoMap;

    private Map<Element, Integer> elementTokenScore;
    private Element element;
    private Set<Match<Element>> matchingElements;


    public TokenRepo() {
        this.repoMap = new ConcurrentHashMap<>();
    }

    public void initializeElementScore(Element element) {
        elementTokenScore = new ConcurrentHashMap<>();
        this.element = element;
        this.matchingElements = new HashSet<>();
    }

    public void put(Token token) {

        ElementClassification elementClassification = token.getElement().getElementClassification();
        Repo repo = repoMap.get(elementClassification);

        if (repo == null) {
            repo = new Repo(elementClassification);
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

    public Set<Match<Element>> getThresholdMatching(Token token) {
        Set<Element> matchElements = this.get(token);
        if (matchElements != null) {
            matchElements.forEach(matchElement -> {
                int score = elementTokenScore.getOrDefault(matchElement, 0) + 1;
                elementTokenScore.put(matchElement, score);
                // Element Score above threshold
                double elementScore = element.getScore(score, matchElement);

                if (elementScore > element.getThreshold()) {
                    // TODO: Remove multiple matches for same Element pair
                    matchingElements.add(new Match<>(element, matchElement, elementScore));
                }

            });
        }
        return matchingElements;
    }


    private class Repo {

        MatchType matchType;

        Map<Object, Set<Element>> tokenElementSet;

        TreeSet<Token> tokenBT;

        Repo(ElementClassification elementClassification) {
            this.matchType = elementClassification.getElementType().getMatchType();
            switch (matchType) {
                case EQUALITY:
                    tokenElementSet = new ConcurrentHashMap<>();
                    break;
                case NEAREST_NEIGHBOURS:
                    tokenBT = new TreeSet<>(Token.byValue);
                    break;
            }
        }

        void put(Token token, Element element) {
            switch (matchType) {
                case EQUALITY:
                    Set<Element> elements = tokenElementSet.getOrDefault(token.getValue(), new HashSet<>());
                    elements.add(element);
                    tokenElementSet.put(token.getValue(), elements);
                    break;
                case NEAREST_NEIGHBOURS:
                    tokenBT.add(token);

            }
        }

        Set<Element> get(Token token) {
            switch (matchType) {
                case EQUALITY:
                    return tokenElementSet.get(token.getValue());
                case NEAREST_NEIGHBOURS:
                    TokenRange tokenRange = new TokenRange(token, token.getElement().getThreshold());
                    return tokenBT.subSet(tokenRange.lower, true, tokenRange.higher, true)
                            .stream()
                            .map(Token::getElement).collect(Collectors.toSet());

            }
            return null;
        }
    }

    private class TokenRange {

        private final Token lower;
        private final Token higher;
        private static final double DATE_SCALE_FACTOR = 1.1;


        TokenRange(Token token, double pct) {
            Object value = token.getValue();
            if (value instanceof Double) {
                this.lower = new Token(getLower((Double) value, pct).doubleValue(), token.getElement());
                this.higher = new Token(getHigher((Double) value, pct).doubleValue(), token.getElement());
            } else if (value instanceof Integer) {
                this.lower = new Token(getLower((Integer) value, pct).intValue(), token.getElement());
                this.higher = new Token(getHigher((Integer) value, pct).intValue(), token.getElement());
            } else if (value instanceof Long) {
                this.lower = new Token(getLower((Long) value, pct).longValue(), token.getElement());
                this.higher = new Token(getHigher((Long) value, pct).longValue(), token.getElement());
            } else if (value instanceof Float) {
                this.lower = new Token(getLower((Float) value, pct).floatValue(), token.getElement());
                this.higher = new Token(getHigher((Float) value, pct).floatValue(), token.getElement());
            } else if (value instanceof Date) {
                this.lower = getDateToken(getLower(((Date) value).getTime(), pct * DATE_SCALE_FACTOR), token);
                this.higher = getDateToken(getHigher(((Date) value).getTime(), pct * DATE_SCALE_FACTOR), token);
            } else {
                throw new RuntimeException("Data Type not supported");
            }
        }

        private Token getDateToken(Number number, Token token) {
            return new Token(new Date(number.longValue()), token.getElement());
        }

        private Number getLower(Number number, double pct) {
            Double dnum = number.doubleValue();
            Double pctVal = dnum * (1.0 - pct);
            return dnum - pctVal;
        }

        private Number getHigher(Number number, double pct) {
            Double dnum = number.doubleValue();
            Double pctVal = dnum * (1.0 - pct);
            return dnum + pctVal;
        }

    }

}
