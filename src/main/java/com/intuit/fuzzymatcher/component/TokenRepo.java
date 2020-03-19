package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementClassification;
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

        TreeSet<Token> tokenBinaryTree;

        Repo(MatchType matchType) {
            this.matchType = matchType;
            switch (matchType) {
                case EQUALITY:
                    tokenElementSet = new ConcurrentHashMap<>();
                    break;
                case NEAREST_NEIGHBOURS:
                    tokenBinaryTree = new TreeSet<>(Token.byValue);
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
                    tokenBinaryTree.add(token);

            }
        }

        Set<Element> get(Token token) {
            switch (matchType) {
                case EQUALITY:
                    return tokenElementSet.get(token.getValue());
                case NEAREST_NEIGHBOURS:
                    TokenRange tokenRange = new TokenRange(token, token.getElement().getNeighborhoodRange());
                    return tokenBinaryTree.subSet(tokenRange.lower, true, tokenRange.higher, true)
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
                throw new MatchException("Data Type not supported");
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
