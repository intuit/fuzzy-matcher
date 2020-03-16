package com.intuit.fuzzymatcher.component;


import com.intuit.fuzzymatcher.domain.*;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Starts the Matching process by element level matching and aggregates the results back
 * This uses the ScoringFunction defined at each Document to get the aggregated Document score for matched Elements
 */
public class DocumentMatch {

    private final TokenRepo tokenRepo;

    public DocumentMatch() {
        tokenRepo = new TokenRepo();
    }

    /**
     * Executes matching of a document stream
     *
     * @param documents Stream of Document objects
     * @return Stream of Match of Document type objects
     */
    public Stream<Match<Document>> matchDocuments(Stream<Document> documents) {

        Stream<Match<Document>> documentMatch =  documents.flatMap(document -> {
            Set<Element> elements = document.getPreProcessedElement();
            Set<Match<Element>> eleMatches =   elements.stream().flatMap(element -> findElementMatches(element).stream()).collect(Collectors.toSet());
            return documentThresholdMatching(document, eleMatches);
        });

        return documentMatch;
    }

    private Stream<Match<Document>> documentThresholdMatching(Document document, Set<Match<Element>> matchingElements) {
        Map<Document, List<Match<Element>>> mathes =  matchingElements.stream()
                .collect(Collectors.groupingBy(matchElement -> matchElement.getMatchedWith().getDocument()));

        Stream<Match<Document>> result = mathes.entrySet().stream().flatMap(matchEntry -> {

            List<Score> childScoreList = matchEntry.getValue()
                    .stream()
                    .map(d -> d.getScore())
                    .collect(Collectors.toList());
            //System.out.println(Arrays.toString(childScoreList.toArray()));
            Match<Document> leftMatch = new Match<Document>(document, matchEntry.getKey(), childScoreList);

            // Document match Found
            if (leftMatch.getScore().getResult() > leftMatch.getData().getThreshold()) {

                if (BooleanUtils.isNotFalse(matchEntry.getKey().isSource())) {
                    Match<Document> rightMatch = new Match<Document>(matchEntry.getKey(), document, childScoreList);
                    return Stream.of(leftMatch, rightMatch);
                }
                return Stream.of(leftMatch);
            } else {
                return Stream.empty();
            }
        });

        return result;
    }

    private Set<Match<Element>> findElementMatches(Element element ) {
        Set<Match<Element>> matchElements = new HashSet<>();
        Map<Element, Integer> elementTokenScore = new HashMap<>();

        element.getTokens()
                .filter(token -> BooleanUtils.isNotFalse(element.getDocument().isSource()))
                .forEach(token -> {
                    elementThresholdMatching(token, elementTokenScore, matchElements);
                });

        element.getTokens().forEach(token -> tokenRepo.put(token));

        return matchElements;
    }

    private void elementThresholdMatching(Token token,  Map<Element, Integer> elementTokenScore, Set<Match<Element>> matchingElements) {
        Set<Element> matchElements = tokenRepo.get(token);
        Element element  = token.getElement();

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
