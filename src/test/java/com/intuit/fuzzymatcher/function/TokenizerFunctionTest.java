package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intuit.fuzzymatcher.domain.ElementType.*;
import static com.intuit.fuzzymatcher.function.TokenizerFunction.*;

public class TokenizerFunctionTest {
    @Test
    public void itShouldGetNGramTokenizer_Success(){
        String value = "james_parker";
        Element elem = new Element.Builder().setType(EMAIL).setValue(value).createElement();
        Assert.assertEquals((value.length()-2)<0? 1: (value.length()-2), triGramTokenizer().apply(elem).count());
    }

    @Test
    public void itShouldReturnNGramTokenForSmallStrings_Success(){
        String value = "jp";
        Element elem = new Element.Builder().setType(EMAIL).setValue(value).createElement();
        Assert.assertEquals(1, triGramTokenizer().apply(elem).count());
    }

    @Test
    public void itShouldGetWordTokenizerForAddress_Success(){
        String value = "123 new Street, minneapolis mn";
        Element elem = new Element.Builder().setType(ADDRESS).setValue(value).createElement();
        Assert.assertEquals(5, wordTokenizer().apply(elem).count() );
    }

    @Test
    public void itShouldGetWordTokenizerForName_Success(){
        String value = "James G. Parker";
        Element elem = new Element.Builder().setType(NAME).setValue(value).createElement();
        Assert.assertEquals(3, wordTokenizer().apply(elem).count() );
    }

    @Test
    public void itShouldGetValueTokenizer_Success(){
        String value = "0011234567890";
        Element elem = new Element.Builder().setType(PHONE).setValue(value).createElement();
        Assert.assertEquals(1, valueTokenizer().apply(elem).count() );
        Assert.assertEquals("1234567890", valueTokenizer().apply(elem).findFirst().get().getValue() );
    }

    @Test
    public void itShouldGetValueTokenizerForNumber_Success(){
        String value = "123.34";
        Element elem = new Element.Builder().setType(NUMBER).setValue(value).createElement();
        Assert.assertEquals(1, valueTokenizer().apply(elem).count() );
        Assert.assertEquals("123.34", valueTokenizer().apply(elem).findFirst().get().getValue() );
    }

    @Test
    public void itShouldTestExtraSpaceAndSpecialCharacters_Success(){
        String value = "12/3,    new     Street, minneapolis-   mn";
        Element elem = new Element.Builder().setType(ADDRESS).setValue(value).createElement();
        Assert.assertEquals(5, wordTokenizer().apply(elem).count() );
    }


    @Test
    public void itShouldGetNGramTokenizerLongString(){
        String value = "thisStringIsUsedForTestingAReallyHumungusExtremlyLongAndLargeStringForEnsuringThePerformanceOfTheLuceneTokenizerFunction";
        Element elem = new Element.Builder().setType(EMAIL).setValue(value).createElement();
        Assert.assertEquals((value.length()-2)<0? 1: (value.length()-2), triGramTokenizer().apply(elem).count());

        String value2 = "thisStringIsUsedForTestingAReallyHumungusExtremlyLongAndLargeString";
        Element elem2 = new Element.Builder().setType(EMAIL).setValue(value2).createElement();
        Assert.assertEquals((value2.length()-2)<0? 1: (value2.length()-2), triGramTokenizer().apply(elem2).count());
    }

    @Test
    public void itShouldGetWordSoundexEncodeTokenizerForAddress() {
        String value = "123 new Street 23rd Ave";
        Element elem = new Element.Builder().setType(ADDRESS).setValue(value).createElement();
        Stream<Token> resultStream = wordSoundexEncodeTokenizer().apply(elem);
        List<Token> results = resultStream.collect(Collectors.toList());
        Assert.assertEquals(5, results.size() );
        Assert.assertEquals("123", results.get(0).getValue());
        Assert.assertEquals("N000", results.get(1).getValue());
        Assert.assertEquals("S363", results.get(2).getValue());
        Assert.assertEquals("23rd", results.get(3).getValue());
    }

    @Test
    public void itShouldGetWordSoundexEncodeTokenizerForName() {
        String value1 = "Stephen Wilkson";
        Element elem1 = new Element.Builder().setType(NAME).setValue(value1).createElement();
        Stream<Token> tokenStream1 = wordSoundexEncodeTokenizer().apply(elem1);
        List<Token> results1 = tokenStream1.collect(Collectors.toList());
        Assert.assertEquals("S315", results1.get(0).getValue());
        Assert.assertEquals("W425", results1.get(1).getValue());

        String value2 = "Steven Wilson";
        Element elem2 = new Element.Builder().setType(NAME).setValue(value2).createElement();
        Stream<Token> tokenStream2 = wordSoundexEncodeTokenizer().apply(elem2);
        List<Token> results2 = tokenStream2.collect(Collectors.toList());
        Assert.assertEquals("S315", results2.get(0).getValue());
        Assert.assertEquals("W425", results2.get(1).getValue());

    }


}
