package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementClassification;
import com.intuit.fuzzymatcher.domain.MatchType;
import com.intuit.fuzzymatcher.domain.Token;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class TokenRepo {

    private Map<ElementClassification, Repo> repoMap;


    public TokenRepo() {
        this.repoMap = new ConcurrentHashMap<>();
    }

    public void put(Token token) {

        ElementClassification elementClassification = token.getElement().getElementClassification();
        Repo repo = repoMap.get(elementClassification);

        if (repo == null) {
            repo = new Repo(elementClassification);
            repoMap.put(elementClassification, repo);
        }
        repo.put(token.getEncodedValue(), token.getElement());
    }


    public boolean contains(Token token) {
        Repo repo = repoMap.get(token.getElement().getElementClassification());
        return (repo != null && repo.containsKey(token.getEncodedValue()));
    }

    public Set<Element> get(Token token) {
        Repo repo = repoMap.get(token.getElement().getElementClassification());
        if (repo != null) {
            return repo.get(token.getEncodedValue());
        }
        return null;
    }


    private class Repo<T> {

        MatchType matchType;

        Map<T, Set<Element>> tokenElementSet;

        Set<T> tokenBT;

        Repo(ElementClassification elementClassification) {
            this.matchType = elementClassification.getElementType().getMatchType();
            switch (matchType) {
                case SOUNDEX:
                case EQUALITY:
                    tokenElementSet = new ConcurrentHashMap<>();
                    break;
                case NEAREST_NEIGHBOURS:
                    tokenBT = new TreeSet<>();
                    break;
            }
        }

        void put(T tokenValue, Element element) {
            switch (matchType) {
                case SOUNDEX:
                case EQUALITY:
                    Set<Element> elements = tokenElementSet.getOrDefault(tokenValue, new HashSet<>());
                    elements.add(element);
                    tokenElementSet.put(tokenValue, elements);
                    break;
                case NEAREST_NEIGHBOURS:
                    throw new RuntimeException("Not implemented");

            }
        }

        boolean containsKey(T tokenValue) {
            switch (matchType) {
                case SOUNDEX:
                case EQUALITY:
                    return tokenElementSet.containsKey(tokenValue);
                case NEAREST_NEIGHBOURS:
                    throw new RuntimeException("Not implemented");

            }
            return false;
        }

        Set<Element> get(T tokenValue) {
            switch (matchType) {
                case SOUNDEX:
                case EQUALITY:
                    return tokenElementSet.get(tokenValue);
                case NEAREST_NEIGHBOURS:
                    throw new RuntimeException("Not implemented");

            }
            return null;
        }
    }

}
