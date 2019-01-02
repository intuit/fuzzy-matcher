package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.AppConfig;
import com.intuit.fuzzymatcher.component.TokenMatch;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.Score;
import com.intuit.fuzzymatcher.domain.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Any;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.ElementType.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ScoringFunctionTest {

    @Test
    public void itShouldGiveAverageScore_Success(){
        Document document1 = getMockDocument(4L, 0L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2,
                new Match(getMockElement(1.0), null, 0.66),
                new Match(getMockElement(2.0), null, 1.0));

        Score score = ScoringFunction.getAverageScore().apply(match);
        Assert.assertEquals(0.41, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGiveAverageScoreWithEmptyFields_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2,
                new Match(getMockElement(1.0), null, 0.5),
                new Match(getMockElement(2.0), null, 1.0));

        Score score = ScoringFunction.getAverageScore().apply(match);
        Assert.assertEquals(.62, score.getResult(), 0.01);
    }


    @Test
    public void itShouldGetExponentialScoring_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2,
                new Match(getMockElement(1.0), null, 1.0),
                new Match(getMockElement(2.0), null, 1.0));

        Score score = ScoringFunction.getExponentialAverageScore().apply(match);
        Assert.assertEquals(.79, score.getResult(), 0.01);
    }

    @Test
    public void itShouldGiveWeightedAverageScore_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2,
                new Match(getMockElement(1.0), null, 1.0),
                new Match(getMockElement(2.0), null, 1.0));
        Score score = ScoringFunction.getWeightedAverageScore().apply(match);
        Assert.assertEquals(.8, score.getResult(), 0.01);
    }


    @Test
    public void itShouldGetExponentialWeightedScoring_Success(){
        Document document1 = getMockDocument(4L, 2L);
        Document document2 = getMockDocument(4L, 0L);

        Match<Document> match = getMockMatch(document1, document2,
                new Match(getMockElement(1.0), null, 1.0),
                new Match(getMockElement(2.0), null, 1.0));

        Score score = ScoringFunction.getExponentialWeightedAverageScore().apply(match);
        Assert.assertEquals(.86, score.getResult(), 0.01);
    }


    @Test
    public void itShouldGetJaccardScoring_Success(){
        Element element1 = new Element.Builder().setType(ADDRESS).setValue("123 new st.").createElement();
        Element element2 = new Element.Builder().setType(ADDRESS).setValue("123 new street. Minneapolis MN").createElement();

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

        Stream<Token> tokens = Stream.concat(element1.getType().getTokenizerFunction().apply(element1),element2.getType().getTokenizerFunction().apply(element2));
        TokenMatch tokenMatch = new TokenMatch();
        Stream<Match<Token>> matches = tokenMatch.matchTokens(tokens);
        List<Score> childResults = matches.filter(d->d.getData().getElement().getDocument().getKey().equals("1"))
                .map(x -> x.getScore()).collect(Collectors.toList());
        Match<Element> match = new Match<>(element1,element2,childResults);
        Score score = ScoringFunction.getJaccardScore().apply(match);
        Assert.assertEquals(0.6,score.getResult(),0.01);
    }

    @Test
    public void itShouldNotScoreMoreThanOne_Success() {
        Element element1 = new Element.Builder().setType(ADDRESS).setValue("325 NS 3rd Street Ste 567 Miami FL 33192").createElement();
        Element element2 = new Element.Builder().setType(ADDRESS).setValue("325 NS 3rd Street Ste 567 Miami FL 33192").createElement();

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
        TokenMatch tokenMatch = new TokenMatch();
        Stream<Match<Token>> tokenMatches = tokenMatch.matchTokens(tokens);
        List<Score> childResults = tokenMatches.filter(d->d.getData().getElement().getDocument().getKey().equals("1"))
                .map(x -> x.getScore()).collect(Collectors.toList());
        Match<Element> match = new Match<>(element1,element2,childResults);
        Score score = match.getData().getScoringFunction().apply(match);
        Assert.assertEquals(1.0,score.getResult(),0.0);
    }

    private Document getMockDocument(long childCount, long emptyCount) {
        Document doc = mock(Document.class);
        when(doc.getChildCount(any())).thenReturn(childCount);
        when(doc.getUnmatchedChildCount(any())).thenReturn(emptyCount);
        return doc;
    }

    private Element getMockElement(double weight) {
        Element element = mock(Element.class);
        when(element.getWeight()).thenReturn(weight);
        return element;
    }

    private Match getMockMatch(Document doc1, Document doc2, Match... matches) {
        List<Score> childScores = Stream.of(matches).map(Match::getScore).collect(Collectors.toList());
        return new Match<>(doc1, doc2, childScores);
    }
}
