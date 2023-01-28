package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScoringFunctionTest {

    @Test
    public void itShouldGiveWeightedAverageScore_Success(){
        Document document1 = mock(Document.class);
        Document document2 = mock(Document.class);

        when(document1.getWeightedChildCount(document2)).thenReturn(5.0);
        when(document1.getUnmatchedChildWeight(document2)).thenReturn(2.0);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 1.0),
                new Match(getMockElement(2.0, 1), null, 1.0));
        Score score = ScoringFunction.getWeightedAverageScore().apply(match, childScores);
        Assert.assertEquals(.8, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGetExponentialWeightedScoring_Success(){
        Document document1 = mock(Document.class);
        Document document2 = mock(Document.class);

        when(document1.getWeightedChildCount(document2)).thenReturn(5.0);
        when(document1.getUnmatchedChildWeight(document2)).thenReturn(2.0);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 1.0),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getExponentialWeightedAverageScore().apply(match, childScores);
        Assert.assertEquals(.86, score.getResult(), 0.01);
    }

    private Element getMockElement(double weight, long childCount) {
        Element element = mock(Element.class);
        when(element.getWeight()).thenReturn(weight);
        List<Token> tokens =  LongStream.range(0, childCount).mapToObj(l -> getMockToken()).collect(Collectors.toList());
        when(element.getTokens()).thenReturn(tokens);
        return element;
    }

    private Token getMockToken() {
        Token token = mock(Token.class);
        return token;
    }

    private Match getMockMatch(Document doc1, Document doc2) {
        return new Match<>(doc1, doc2);
    }

    private List<Score> getMockChildScores(Match... matches) {
        return Stream.of(matches).map(Match::getScore).collect(Collectors.toList());
    }
}
