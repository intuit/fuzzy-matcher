package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.ElementType.*;
import static org.mockito.Matchers.any;
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
    public void itShouldGiveAverageScoreWithElements_Success(){
        Element element1 = getMockElement(1L, 7);
        Element element2 = getMockElement(1L, 5);

        Match<Document> match = getMockMatch(element1, element2);
        List<Score> childScores = getMockChildScores(new Match(getMockToken(), null, 0.66),
                new Match(getMockToken(), null, 0.78),
                new Match(getMockToken(), null, 0.95),
                new Match(getMockToken(), null, 1.0));

        Score score = ScoringFunction.getAverageScore().apply(match, childScores);
        Assert.assertEquals(0.48, score.getResult(), 0.01);
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
    public void itShouldGiveSimpleAverageScoreWithElements_Success(){
        Element element1 = getMockElement(1L, 7);
        Element element2 = getMockElement(1L, 5);

        Match<Document> match = getMockMatch(element1, element2);
        List<Score> childScores = getMockChildScores(new Match(getMockToken(), null, 0.66),
                new Match(getMockToken(), null, 0.78),
                new Match(getMockToken(), null, 0.95),
                new Match(getMockToken(), null, 1.0));

        Score score = ScoringFunction.getSimpleAverageScore().apply(match, childScores);
        Assert.assertEquals(0.48, score.getResult(), 0.01);
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

    @Test
    @Ignore
    public void itShouldNotScoreMoreThanOne_Success() {
        Element<String> element1 = new Element.Builder().setType(ADDRESS).setValue("325 NS 3rd Street Ste 567 Miami FL 33192").createElement();
        Element<String> element2 = new Element.Builder().setType(ADDRESS).setValue("325 NS 3rd Street Ste 567 Miami FL 33192").createElement();

        Document document1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James P").createElement())
                .addElement(element1)
                .addElement(new Element.Builder().setType(PHONE).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument();
        Document document2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(element2)
                .addElement(new Element.Builder().setType(PHONE).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james.parker@gmail.com").setThreshold(0.5).createElement())
                .createDocument();

        element1.setDocument(document1);
        element2.setDocument(document2);

        Stream<Token> tokens = Stream.concat(element1.getTokenizerFunction().apply(element1),element2.getTokenizerFunction().apply(element2));
//        TokenMatch tokenMatch = new TokenMatch();
//        Stream<Match<Token>> tokenMatches = tokenMatch.matchTokens(element1.getElementClassification(), tokens);
//        List<Score> childResults = tokenMatches.filter(d->d.getData().getElement().getDocument().getKey().equals("1"))
//                .map(x -> x.getScore()).collect(Collectors.toList());
//        Match<Element> match = new Match<>(element1,element2);
//        Score score = match.getData().getScoringFunction().apply(match, childResults);
//        Assert.assertEquals(1.0,score.getResult(),0.0);
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

    private Match getMockMatch(Element elem1, Element elem2) {
        long max = Math.max(elem1.getTokens().size(), elem2.getTokens().size());
        when(elem1.getChildCount(any())).thenReturn(max);
        return new Match<>(elem1, elem2);
    }

    private List<Score> getMockChildScores(Match... matches) {
        return Stream.of(matches).map(Match::getScore).collect(Collectors.toList());
    }
}
