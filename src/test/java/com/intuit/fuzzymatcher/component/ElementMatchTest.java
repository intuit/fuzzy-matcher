package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Stream;

public class ElementMatchTest {

    //private ElementMatch elementMatch = new ElementMatch();

    @Test
    @Ignore
    public void itShouldMatchElements() {
        Stream<Document> documentStream = Stream.of(
                getDocWithName("1", "Rodrigue Rodrigues"),
                getDocWithName("2", "Rodrigues, Rodrigue"));

        Stream<Element> elements = documentStream.flatMap(d -> d.getDistinctNonEmptyElements());

//        ElementClassification elementClassification = new ElementClassification(ElementType.NAME, "",
//                ElementType.NAME.getMatchOptimizerFunction());
//
//        Stream<Match<Element>> matchResults = elementMatch.matchElements(elementClassification, elements);
//        matchResults.forEach(match -> {
//            System.out.println(match.getResult());
//            Assert.assertTrue("Match less than 1.0", match.getResult() <= 1.0);
//        });


    }

    private Document getDocWithName(String DocKey, String value) {
        return new Document.Builder(DocKey)
                .addElement(new Element.Builder()
                        .setType(ElementType.NAME)
                        .setValue(value)
                        .createElement())
                .createDocument();
    }


    public void testMatch() {
        Token t1 = new Token("Rodrigue", null);
        Token t2 = new Token("Rodrigues", null);
        Token t3 = new Token("Rodri", null);
        Token t4 = new Token("Rod", null);

//        Stream<Match> mathStream = Stream.of(
//                new Match(t1, t2, 0.9),
//                new Match(t1, t3, 0.5),
//                new Match(t1, t4, 0.4),
//                new Match(t2, t1, 0.9),
//                new Match(t2, t3, 0.5),
//                new Match(t2, t4, 0.2),
//                new Match(t3, t1, 0.5),
//                new Match(t3, t2, 0.5),
//                new Match(t3, t4, 0.8),
//                new Match(t4, t1, 0.4),
//                new Match(t4, t2, 0.2),
//                new Match(t4, t3, 0.8)
//        );

    }


}
