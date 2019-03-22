package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.Score;
import com.intuit.fuzzymatcher.domain.Token;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Matches at element level with aggregated results from token.
 * This uses the ScoringFunction defined at each element to get the aggregated Element score for matched tokens
 */
public class ElementMatch {

    private static TokenMatch tokenMatch = new TokenMatch();

    public Stream<Match<Element>> matchElements(Stream<Element> elements) {
        Stream<Token> tokenStream = elements.flatMap(Element::getTokens);
        Stream<Match<Token>> matchedTokens = tokenMatch.matchTokens(tokenStream);
        return rollupElementScore(matchedTokens);
    }

    private Stream<Match<Element>> rollupElementScore(Stream<Match<Token>> matchedTokenStream) {

        Map<Element, Map<Element, List<Match<Token>>>> groupBy = matchedTokenStream
                .collect(Collectors.groupingBy((matchToken -> matchToken.getData().getElement()),
                        Collectors.groupingBy(matchToken -> matchToken.getMatchedWith().getElement())));

        return groupBy.entrySet().parallelStream().flatMap(leftElementEntry ->
                leftElementEntry.getValue().entrySet().parallelStream().map(rightElementEntry -> {
                    List<Score> childScoreList = rightElementEntry.getValue()
                            .stream().map(d -> d.getScore())
                            .collect(Collectors.toList());

                    return new Match<Element>(leftElementEntry.getKey(), rightElementEntry.getKey(), childScoreList);
                }).filter(match -> match.getResult() > match.getData().getThreshold()));
    }


}
