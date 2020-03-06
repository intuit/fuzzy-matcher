package com.intuit.fuzzymatcher.component;


import com.intuit.fuzzymatcher.domain.*;
import org.apache.commons.codec.language.Soundex;
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
    public Stream<Match<Document>> matchDocuments(Stream<Document> documents) {
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
                            Match<Document> leftMatch = new Match<Document>(leftDocumentEntry.getKey(), rightDocumentEntry.getKey(), childScoreList);
                            if (BooleanUtils.isNotFalse(rightDocumentEntry.getKey().isSource())) {
                                Match<Document> rightMatch = new Match<Document>(rightDocumentEntry.getKey(), leftDocumentEntry.getKey(), childScoreList);
                                return Stream.of(leftMatch, rightMatch);
                            }
                            return Stream.of(leftMatch);
                        }))
                .filter(match -> match.getResult() > match.getData().getThreshold());
    }


    UniqueTokenRepo uniqueTokenRepo = new UniqueTokenRepo();

    public Stream<Match<Document>> matchDocumentsLinear(Stream<Document> documents) {
        System.out.println("Starting Linear Match");

        List<Match<Element>> elementMatches = new ArrayList<>();

        documents.forEach(document -> {

            Set<Element> elements = document.getPreProcessedElement();

            elements.forEach(element -> {
                List<Token> tokens = element.getTokens().collect(Collectors.toList());

                RepoScore repoScore = new RepoScore();
                tokens.forEach(token -> {
                    String encodedValue = getEncodedToken((String) token.getValue());

                    // duplicate token found, create match
                    if (uniqueTokenRepo.contains(element.getElementClassification(), encodedValue)) {
                        Set<Element> matchElements = uniqueTokenRepo.get(element.getElementClassification(), encodedValue);
                        matchElements.forEach(matchElement -> {
                            String key = matchElement.getDocument().getKey();
                            repoScore.increament(key, 1.0);
                            // Element Score above threshold
                            double elementScore = elementScore(repoScore.get(key), element.getChildCount(matchElement));

                            if (elementScore > element.getThreshold()) {
                                elementMatches.add(new Match<>(element, matchElement, elementScore));
                            }

                        });
                    }
                    uniqueTokenRepo.add(element.getElementClassification(), element, encodedValue);

                });
            });

        });
        return rollupDocumentScore(elementMatches.parallelStream());
    }

    Soundex soundex = new Soundex();

    private String getEncodedToken(String value) {
        String code = soundex.encode(value);
        if (code.equals("")) {
            code = value;
        }
        //System.out.println(value + " -> " + code);
        return code;
    }

    private double elementScore(double matchTokens, double totalTokens) {
        return matchTokens / totalTokens;
    }


    private class RepoScore {
        private Map<String, Double> repoScore = new ConcurrentHashMap<>();

        public void increament(String key, double value) {
            Double prevScore = repoScore.getOrDefault(key, 0.0);
            repoScore.put(key, prevScore + value);
        }

        public void put(String key, double value) {
            repoScore.put(key, value);
        }

        public Double get(String key) {
            return repoScore.get(key);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            repoScore.forEach((key, value) -> {
                builder.append(key + "->" + value);
                builder.append("\t");
            });
            return builder.toString();
        }

    }

    private class UniqueTokenRepo {
        private Map<ElementClassification, Map<String, Set<Element>>> uniqueTokenRepo = new ConcurrentHashMap<>();

        public void add(ElementClassification elementClassification, Element element, String value) {
            Map<String, Set<Element>> uniqueValues = uniqueTokenRepo.getOrDefault(elementClassification, new ConcurrentHashMap<>());
            Set<Element> keys = uniqueValues.getOrDefault(value, new HashSet<>());
            keys.add(element);
            uniqueValues.put(value, keys);
            uniqueTokenRepo.put(elementClassification, uniqueValues);

        }

        public boolean contains(ElementClassification elementClassification, String value) {
            Map<String, Set<Element>> uniqueValues = uniqueTokenRepo.get(elementClassification);
            return (uniqueValues != null && uniqueValues.containsKey(value));
        }

        public Set<Element> get(ElementClassification elementClassification, String value) {
            Map<String, Set<Element>> uniqueValues = uniqueTokenRepo.get(elementClassification);
            if (uniqueValues != null) {
                return uniqueValues.get(value);
            }
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("test start");

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
        Stream<Match<Document>> matches =  documentMatch.matchDocumentsLinear(sourceData.stream());

        Map<Document, List<Match<Document>>> result = matches.collect(Collectors.groupingBy(Match::getData));
        result.entrySet().forEach(entry -> {
            entry.getValue().forEach(match -> {
                System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
            });
        });

    }

}
