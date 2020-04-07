package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Match;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
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

    /**
     * Use this for De-duplication of data, where for a given list of documents it finds duplicates
     * Data is aggregated by a given Document Id
     *
     * @param documents the list of documents to match against
     * @return a set containing the grouping of all relevant matches. So if A matches B, and B matches C. They will be grouped together
     */
    public Set<Set<Match<Document>>> applyMatchByGroups(List<Document> documents) {
        DocumentMatch documentMatch = new DocumentMatch();
        Map<String, List<Match<Document>>> matchByKey = documentMatch.matchDocuments(documents.stream())
                .collect(Collectors.groupingBy(match -> match.getData().getKey()));

        Set<String> docKeys = new HashSet<>(matchByKey.keySet());
        Set<Set<Match<Document>>> result = new HashSet<>();

        docKeys.forEach(key -> {
            Set<Match<Document>> matchGroups = new HashSet<>();
            groupSimilar(matchByKey, key, matchGroups);
            if (CollectionUtils.isNotEmpty(matchGroups)) {
                result.add(matchGroups);
            }
        });
        return result;
    }

    private void groupSimilar(Map<String, List<Match<Document>>> matchMap, String key, Set<Match<Document>> matchGroups) {
        List<Match<Document>> matches = matchMap.get(key);
        if (matches == null) {
            return;
        }
        matchMap.remove(key);

        matches.forEach(match -> {
            if (!containsMatch(matchGroups, match)) {
                matchGroups.add(match);
            }
            String matchedWithKey = match.getMatchedWith().getKey();
            groupSimilar(matchMap, matchedWithKey, matchGroups);
        });
    }

    private boolean containsMatch(Set<Match<Document>> matchGroups, Match<Document> match) {
        return matchGroups.stream()
                .anyMatch(m -> m.getData().getKey().equals(match.getMatchedWith().getKey())
                        && m.getMatchedWith().getKey().equals(match.getData().getKey())
                );
    }
}
