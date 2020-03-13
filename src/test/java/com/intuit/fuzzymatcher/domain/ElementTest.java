package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.function.ScoringFunction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intuit.fuzzymatcher.domain.ElementType.NAME;

public class ElementTest {

    MatchService matchService = new MatchService();

    @Test
    @Ignore
    public void itShouldSetScoringFunction() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Franco Parker").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .createDocument();
        List<Document> inputData = new ArrayList<>();
        inputData.add(d1);
        inputData.add(d2);

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(1.0, result.get(d1).get(0).getResult(), .01);

    }

    static ScoringFunction getFullScore() {
        return (match, childScores) ->
                new Score(1.0, match);
    }
}
