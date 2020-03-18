package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;

public class ElementMatch {

    private final TokenRepo tokenRepo;

    public ElementMatch() {
        this.tokenRepo = new TokenRepo();
    }

    public Set<Match<Element>> matchElement(Element element) {
        Set<Match<Element>> matchElements = new HashSet<>();
        Map<Element, Integer> elementTokenScore = new HashMap<>();

        List<Token> tokens = element.getTokens();
        tokens.stream()
                .filter(token -> BooleanUtils.isNotFalse(element.getDocument().isSource()))
                .forEach(token -> {
                    elementThresholdMatching(token, elementTokenScore, matchElements);
                });

        tokens.forEach(token -> tokenRepo.put(token));

        return matchElements;
    }

    private void elementThresholdMatching(Token token, Map<Element, Integer> elementTokenScore, Set<Match<Element>> matchingElements) {
        Set<Element> matchElements = tokenRepo.get(token);
        Element element = token.getElement();

        // Token Match Found
        if (matchElements != null) {
            matchElements.forEach(matchElement -> {
                int score = elementTokenScore.getOrDefault(matchElement, 0) + 1;
                elementTokenScore.put(matchElement, score);
                // Element Score above threshold
                double elementScore = element.getScore(score, matchElement);

                // Element match Found
                if (elementScore > element.getThreshold()) {
                    Match<Element> elementMatch = new Match<>(element, matchElement, elementScore);
                    matchingElements.remove(elementMatch);
                    matchingElements.add(elementMatch);
                }
            });
        }
    }
}
