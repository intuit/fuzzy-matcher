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

    private final ElementMatch elementMatch;

    public DocumentMatch() {
        this.elementMatch = new ElementMatch();
    }

    /**
     * Executes matching of a document stream
     *
     * @param documents Stream of Document objects
     * @return Stream of Match of Document type objects
     */
    public Stream<Match<Document>> matchDocuments(Stream<Document> documents) {

        Stream<Match<Document>> documentMatch = documents.flatMap(document -> {
            Set<Element> elements = document.getPreProcessedElement();
            Set<Match<Element>> eleMatches = elements.stream()
                    .flatMap(element -> elementMatch.matchElement(element).stream())
                    .collect(Collectors.toSet());
            return documentThresholdMatching(document, eleMatches);
        });

        return documentMatch;
    }

    private Stream<Match<Document>> documentThresholdMatching(Document document, Set<Match<Element>> matchingElements) {
        Map<Document, List<Match<Element>>> matches = matchingElements.stream()
                .collect(Collectors.groupingBy(matchElement -> matchElement.getMatchedWith().getDocument()));

        Stream<Match<Document>> result = matches.entrySet().stream().flatMap(matchEntry -> {

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

}
