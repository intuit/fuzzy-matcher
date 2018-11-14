package com.intuit.fm.component;

import com.intuit.fm.AppConfig;
import com.intuit.fm.domain.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.fm.domain.ElementType.*;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class TokenMatchTest {
    @Test
    public void itShouldMatchTokens_Success(){
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
        Assert.assertEquals(6, matches.collect(Collectors.toList()).size());
    }


    @Test
    public void itShouldMatchTokens_Fail(){
        Element element1 = new Element.Builder().setType(ADDRESS).setValue("456 college raod, Ohio").createElement();
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
        Assert.assertTrue(matches.collect(Collectors.toList()).isEmpty());
    }



    //It checks for the matched tokens whether they are from same key or not.
    @Test
    public void itShouldNotMatchTokensWithSameKey_Success() {
        Element element1 = new Element.Builder().setType(ADDRESS).setValue("123 new Street new street").createElement();
        Element element2 = new Element.Builder().setType(ADDRESS).setValue("123 new Street").createElement();

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
        Assert.assertFalse(matches.anyMatch(d -> d.getData().getElement().getDocument().getKey().equals(d.getMatchedWith().getElement().getDocument().getKey())));
    }
}
