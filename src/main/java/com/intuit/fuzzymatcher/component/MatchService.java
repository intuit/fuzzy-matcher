package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Match;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entry Point for Fuzzy Matching. This class provides different ways to accept Documents for primarily 3 use case
 * <p>
 * 1. De-duplication of data - Where for a given list of documents it finds duplicates
 * 2. Check duplicate for a new data - Where it checks for a new Document a duplicate is present in existing list
 * 3. Check duplicates for bulk inserts - Similar to 2, where a list of new Documents is checked against existing
 * <p>
 * This also has similar implementation to aggregate results in different formats.
 */
public class MatchService {

    /**
     * Use this for De-duplication of data, where for a given list of documents it finds duplicates
     * Data is aggregated by a given Document
     *
     * @param documents the list of documents to match against
     * @return a map containing the grouping of each document and its corresponding matches
     */
    public Map<Document, List<Match<Document>>> applyMatchOld(List<Document> documents) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocumentsOld(documents.parallelStream())
                .collect(Collectors.groupingBy(Match::getData));
    }

    public Map<Document, List<Match<Document>>> applyMatch(List<Document> documents) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocuments(documents.stream())
                .collect(Collectors.groupingBy(Match::getData));
    }

    /**
     * Use this to check duplicates for bulk inserts, where a list of new Documents is checked against existing list
     * Data is aggregated by a given Document
     *
     * @param documents the list of documents to match from
     * @param matchWith the list of documents to match against
     * @return a map containing the grouping of each document and its corresponding matches
     */
    public Map<Document, List<Match<Document>>> applyMatchOld(List<Document> documents, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocumentsOld(Stream.concat(
                documents.parallelStream().map(document -> {
                    document.setSource(true);
                    return document;
                }), matchWith.parallelStream().map(document -> {
                    document.setSource(false);
                    return document;
                })))
                .collect(Collectors.groupingBy(Match::getData));
    }

    public Map<Document, List<Match<Document>>> applyMatch(List<Document> documents, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocuments(Stream.concat(
                matchWith.stream().map(document -> {
                    document.setSource(false);
                    return document;
                }),
                documents.stream().map(document -> {
                    document.setSource(true);
                    return document;
                })))
                .collect(Collectors.groupingBy(Match::getData));
    }

    /**
     * Use this to check duplicate for a new record, where it checks whether a new Document is a duplicate in existing list
     * Data is aggregated by a given Document
     *
     * @param document  the document to match
     * @param matchWith the list of documents to match against
     * @return a map containing the grouping of each document and its corresponding matches
     */
    public Map<Document, List<Match<Document>>> applyMatchOld(Document document, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return applyMatchOld(Arrays.asList(document), matchWith);
    }

    public Map<Document, List<Match<Document>>> applyMatch(Document document, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return applyMatch(Arrays.asList(document), matchWith);
    }

    /**
     * Use this to check duplicate for a new record, where it checks whether a new Document is a duplicate in existing list
     * Data is aggregated by a given Document Id
     *
     * @param document  the document to match
     * @param matchWith the list of documents to match against
     * @return a map containing the grouping of each document id and its corresponding matches
     */
    public Map<String, List<Match<Document>>> applyMatchByDocIdOld(Document document, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return applyMatchByDocIdOld(Arrays.asList(document), matchWith);
    }

    public Map<String, List<Match<Document>>> applyMatchByDocId(Document document, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return applyMatchByDocId(Arrays.asList(document), matchWith);
    }

    /**
     * Use this for De-duplication of data, where for a given list of documents it finds duplicates
     * Data is aggregated by a given Document Id
     *
     * @param documents the list of documents to match against
     * @return a map containing the grouping of each document id and its corresponding matches
     */
    public Map<String, List<Match<Document>>> applyMatchByDocIdOld(List<Document> documents) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocumentsOld(documents.parallelStream())
                .collect(Collectors.groupingBy(match -> match.getData().getKey()));
    }

    public Map<String, List<Match<Document>>> applyMatchByDocId(List<Document> documents) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocuments(documents.stream())
                .collect(Collectors.groupingBy(match -> match.getData().getKey()));
    }

    /**
     * Use this to check duplicates for bulk inserts, where a list of new Documents is checked against existing list
     * Data is aggregated by a given Document Id
     *
     * @param documents the list of documents to match from
     * @param matchWith the list of documents to match against
     * @return a map containing the grouping of each document id and its corresponding matches
     */
    public Map<String, List<Match<Document>>> applyMatchByDocIdOld(List<Document> documents, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocumentsOld(Stream.concat(
                documents.parallelStream().map(document -> {
                    document.setSource(true);
                    return document;
                }), matchWith.parallelStream().map(document -> {
                    document.setSource(false);
                    return document;
                })))
                .collect(Collectors.groupingBy(match -> match.getData().getKey()));
    }

    public Map<String, List<Match<Document>>> applyMatchByDocId(List<Document> documents, List<Document> matchWith) {
        DocumentMatch documentMatch = new DocumentMatch();
        return documentMatch.matchDocuments(Stream.concat(
                matchWith.stream().map(document -> {
                    document.setSource(false);
                    return document;
                }), documents.stream().map(document -> {
                    document.setSource(true);
                    return document;
                })))
                .collect(Collectors.groupingBy(match -> match.getData().getKey()));
    }
}
