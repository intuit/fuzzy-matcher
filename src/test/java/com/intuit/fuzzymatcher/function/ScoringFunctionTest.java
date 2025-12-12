package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScoringFunctionTest {

    @Test
    public void itShouldGiveAverageScore_Success(){
        Document document1 = getMockDocument(4L, 0L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 0.66),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getAverageScore().apply(match, childScores);
        Assert.assertEquals(0.41, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGiveAverageScoreWithEmptyFields_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 0.5),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getAverageScore().apply(match, childScores);
        Assert.assertEquals(.62, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGiveSimpleAverageScore_Success(){
        Document document1 = getMockDocument(4L, 0L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 0.66),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getSimpleAverageScore().apply(match, childScores);
        Assert.assertEquals(0.41, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGetExponentialScoring_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 1.0),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getExponentialAverageScore().apply(match, childScores);
        Assert.assertEquals(.79, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGiveWeightedAverageScore_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 1.0),
                new Match(getMockElement(2.0, 1), null, 1.0));
        Score score = ScoringFunction.getWeightedAverageScore().apply(match, childScores);
        Assert.assertEquals(.8, score.getResult(), 0.01);
    }


    @Test
    public void itShouldGetExponentialWeightedScoring_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2);
        List<Score> childScores = getMockChildScores(new Match(getMockElement(1.0, 1), null, 1.0),
                new Match(getMockElement(2.0, 1), null, 1.0));

        Score score = ScoringFunction.getExponentialWeightedAverageScore().apply(match, childScores);
        Assert.assertEquals(.86, score.getResult(), 0.01);
    }

    private Document getMockDocument(long childCount, long emptyCount) {
        Document doc = mock(Document.class);
        when(doc.getChildCount(any())).thenReturn(childCount);
        when(doc.getUnmatchedChildCount(any())).thenReturn(emptyCount);
        return doc;
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
