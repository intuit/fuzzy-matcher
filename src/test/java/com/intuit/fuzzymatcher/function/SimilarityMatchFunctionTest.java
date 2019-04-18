package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Token;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimilarityMatchFunctionTest {

    @Test
    public void itShouldGetLevenshteinDistance() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("Hello", element);
        Token right = new Token("hell", element);
        double result = SimilarityMatchFunction.levenshtein().apply(left, right);
        Assert.assertEquals(0.8, result, 0.0);
    }

    @Test
    public void itShouldGetJaccardDistance() {
        Element element = getMockElement(1.0, 0.5);
        double result = SimilarityMatchFunction.jaccard().apply(new Token("Hello", element), new Token("hell", element));
        Assert.assertEquals(0.75, result, 0.0);
    }

    @Test
    public void itShouldGetJaroWinklerMatch() {
        Element element = getMockElement(1.0, 0.5);
        double result = SimilarityMatchFunction.jarowinkler().apply(new Token("hello", element), new Token("hell", element));
        Assert.assertEquals(0.96, result, 0.0);
    }

    @Test
    public void itShouldGetSoundex_Success() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("minneapolis", element);
        Token right = new Token("menapoles", element);

        double score = SimilarityMatchFunction.soundex().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetSoundex_Fail() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("minneapolis", element);
        Token right = new Token("minnesota", element);

        double score = SimilarityMatchFunction.soundex().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }


    @Test
    public void itShouldGetPhoneNumber_Success() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("4239765244", element);
        Token right = new Token("14239765244", element);

        double score = SimilarityMatchFunction.phoneNumber().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetPhoneNumber_Fail() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("4239765244", element);
        Token right = new Token("4239765243", element);

        double score = SimilarityMatchFunction.phoneNumber().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }

    @Test
    public void itShouldGetEquality_Success() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("cat", element);
        Token right = new Token("CAT", element);

        double score = SimilarityMatchFunction.equality().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetEquality_Fail() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("cat", element);
        Token right = new Token("bat", element);

        double score = SimilarityMatchFunction.equality().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWith1() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("12.23", element);
        Token right = new Token("12.23", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWithLessThan1() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("80", element);
        Token right = new Token("100", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(0.778, score, 0.001);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWith0() {
        Element element = getMockElement(1.0, 0.5);
        Token left = new Token("1000", element);
        Token right = new Token("80", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(0.0, score, 0.001);
    }

    @Test
    public void itShouldGetDateDifferenceWithinYearSuccessfully() throws ParseException {
        Element element = getMockElement(1.0, 0.9);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date leftDate = sdf.parse("01/01/2020");
        Date rightDate = sdf.parse("01/30/2020");
        Token left = new Token(leftDate, element);
        Token right = new Token(rightDate, element);

        double score = SimilarityMatchFunction.dateDifferenceWithinYear().apply(left, right);
        Assert.assertEquals(0.92, score, 0.001);
    }

    @Test
    public void itShouldGetDateDifferenceWithinYearForSameDate() throws ParseException {
        Element element = getMockElement(1.0, 0.9);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date leftDate = sdf.parse("01/01/2020");
        Token left = new Token(leftDate, element);
        Token right = new Token(leftDate, element);

        double score = SimilarityMatchFunction.dateDifferenceWithinYear().apply(left, right);
        Assert.assertEquals(1.0, score, 0.001);
    }

    @Test
    public void itShouldGetDateDifferenceWithinYearForGreaterThanAYear() throws ParseException {
        Element element = getMockElement(1.0, 0.5);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date leftDate = sdf.parse("01/01/2020");
        Date rightDate = sdf.parse("01/30/2021");
        Token left = new Token(leftDate, element);
        Token right = new Token(rightDate, element);

        double score = SimilarityMatchFunction.dateDifferenceWithinYear().apply(left, right);
        Assert.assertEquals(0.0, score, 0.001);
    }

    private Element getMockElement(double weight, double threshold) {
        Element element = mock(Element.class);
        when(element.getWeight()).thenReturn(weight);
        when(element.getThreshold()).thenReturn(threshold);
        return element;
    }

}
