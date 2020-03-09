package com.intuit.fuzzymatcher.component;


import com.intuit.fuzzymatcher.domain.*;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.ElementType.*;

/**
 * <p>
 * Starts the Matching process by element level matching and aggregates the results back
 * This uses the ScoringFunction defined at each Document to get the aggregated Document score for matched Elements
 */
public class DocumentMatch {

    private static ElementMatch elementMatch = new ElementMatch();

    /**
     * Executes matching of a document stream
     *
     * @param documents Stream of Document objects
     * @return Stream of Match of Document type objects
     */
    public Stream<Match<Document>> matchDocumentsOld(Stream<Document> documents) {
        Stream<Element> elements = documents.flatMap(d -> d.getPreProcessedElement().stream());
        Map<ElementClassification, List<Element>> elementMap = elements.collect(Collectors.groupingBy(Element::getElementClassification));

        List<Match<Element>> matchedElements = new ArrayList<>();
        elementMap.forEach((key, value) -> {
            List<Match<Element>> result = elementMatch.matchElements(key, value.parallelStream()).collect(Collectors.toList());
            matchedElements.addAll(result);
        });

        return rollupDocumentScore(matchedElements.parallelStream());
    }

    private Stream<Match<Document>> rollupDocumentScore(Stream<Match<Element>> matchElementStream) {

        Map<Document, Map<Document, List<Match<Element>>>> groupBy = matchElementStream
                .collect(Collectors.groupingBy(matchElement -> matchElement.getData().getDocument(),
                        Collectors.groupingBy(matchElement -> matchElement.getMatchedWith().getDocument())));

        return groupBy.entrySet().parallelStream().flatMap(leftDocumentEntry ->
                leftDocumentEntry.getValue().entrySet()
                        .parallelStream()
                        .flatMap(rightDocumentEntry -> {
                            List<Score> childScoreList = rightDocumentEntry.getValue()
                                    .stream()
                                    .map(d -> d.getScore())
                                    .collect(Collectors.toList());
                            // System.out.println(Arrays.toString(childScoreList.toArray()));
                            Match<Document> leftMatch = new Match<Document>(leftDocumentEntry.getKey(), rightDocumentEntry.getKey(), childScoreList);
                            if (BooleanUtils.isNotFalse(rightDocumentEntry.getKey().isSource())) {
                                Match<Document> rightMatch = new Match<Document>(rightDocumentEntry.getKey(), leftDocumentEntry.getKey(), childScoreList);
                                return Stream.of(leftMatch, rightMatch);
                            }
                            return Stream.of(leftMatch);
                        }))
                .filter(match -> match.getResult() > match.getData().getThreshold());
    }



    public Stream<Match<Document>> matchDocuments(Stream<Document> documents) {
        System.out.println("Starting Linear Match");
        TokenRepo tokenRepo = new TokenRepo();
        List<Match<Element>> elementMatches = new ArrayList<>();

        documents.forEach(document -> {
            Set<Element> elements = document.getPreProcessedElement();
            elements.forEach(element -> findElementMatches(elementMatches, tokenRepo, element));

        });
        return rollupDocumentScore(elementMatches.parallelStream());
    }

    private void findElementMatches(List<Match<Element>> elementMatches, TokenRepo tokenRepo, Element element ) {
        List<Token> tokens = element.getTokens().collect(Collectors.toList());

        ElementMatchCounter elementMatchCounter = new ElementMatchCounter();
        tokens.forEach(token -> {

            // duplicate token found, create match
            if (BooleanUtils.isNotFalse(element.getDocument().isSource()) && tokenRepo.contains(token)) {
                Set<Element> matchElements = tokenRepo.get(token);
                matchElements.forEach(matchElement -> {
                    elementMatchCounter.put(matchElement);
                    // Element Score above threshold
                    double elementScore = element.getScore(elementMatchCounter.get(matchElement), matchElement);

                    if (elementScore > element.getThreshold()) {
                        // TODO: Remove multiple matches for same Element pair
                        elementMatches.add(new Match<>(element, matchElement, elementScore));
                    }

                });
            }
            tokenRepo.put(token);
        });
    }


    private class ElementMatchCounter {
        private Map<String, Integer> elementMatches = new ConcurrentHashMap<>();

        public void put(Element matchElement) {
            String key = matchElement.getDocument().getKey();
            Integer count = elementMatches.getOrDefault(key, 0);
            elementMatches.put(key, ++count);
        }

        public Integer get(Element matchElement) {
            String key = matchElement.getDocument().getKey();
            return elementMatches.get(key);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            elementMatches.forEach((key, value) -> {
                builder.append(key + "->" + value);
                builder.append("\t");
            });
            return builder.toString();
        }

    }

    public static void main(String[] args) {
        System.out.println("test start");
        testLocal();
    }

    private static void testLocal() {
        List<Document> sourceData = new ArrayList<>();

        sourceData.add(new Document.Builder("S1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .createDocument());

        sourceData.add(new Document.Builder("S3")
                .addElement(new Element.Builder().setType(NAME).setValue("Bond").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("546 Stevens ave, sarasota fl").createElement())
                .createDocument());

        sourceData.add(new Document.Builder("S4")
                .addElement(new Element.Builder().setType(NAME).setValue("William").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 niger Street, dallas tx").createElement())
                .createDocument());

        sourceData.add(new Document.Builder("S2")
                .addElement(new Element.Builder().setType(NAME).setValue("James").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new Street, minneapolis blah").createElement())
                .createDocument());

        DocumentMatch documentMatch = new DocumentMatch();
        Stream<Match<Document>> matches =  documentMatch.matchDocuments(sourceData.stream());

        Map<Document, List<Match<Document>>> result = matches.collect(Collectors.groupingBy(Match::getData));
        result.entrySet().forEach(entry -> {
            entry.getValue().forEach(match -> {
                System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
            });
        });
    }

}
