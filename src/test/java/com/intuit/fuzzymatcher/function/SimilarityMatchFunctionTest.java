package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Token;
import org.junit.Assert;
import org.junit.Test;

public class SimilarityMatchFunctionTest {

    @Test
    public void itShouldGetLevenshteinDistance() {
        Element element = new Element(ElementType.NAME, null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("Hello", element);
        Token right = new Token("hell", element);
        double result = SimilarityMatchFunction.levenshtein().apply(left, right);
        Assert.assertEquals(0.8, result, 0.0);
    }

    @Test
    public void itShouldGetJaccardDistance() {
        Element element = new Element(ElementType.NAME, null,"", 1.0, 0.5,
                null, null, null, null);
        double result = SimilarityMatchFunction.jaccard().apply(new Token("Hello", element), new Token("hell", element));
        Assert.assertEquals(0.75, result, 0.0);
    }

    @Test
    public void itShouldGetJaroWinklerMatch() {
        Element element = new Element(ElementType.NAME, null,"", 1.0, 0.5,
                null, null, null, null);
        double result = SimilarityMatchFunction.jarowinkler().apply(new Token("hello", element), new Token("hell", element));
        Assert.assertEquals(0.96, result, 0.0);
    }

    @Test
    public void itShouldGetSoundex_Success() {
        Element element = new Element(ElementType.ADDRESS, null,"", 1, 0.5,
                null, null, null, null);
        Token left = new Token("minneapolis", element);
        Token right = new Token("menapoles", element);

        double score = SimilarityMatchFunction.soundex().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetSoundex_Fail() {
        Element element = new Element(ElementType.ADDRESS, null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("minneapolis", element);
        Token right = new Token("minnesota", element);

        double score = SimilarityMatchFunction.soundex().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }


    @Test
    public void itShouldGetPhoneNumber_Success() {
        Element element = new Element(ElementType.PHONE, null,"", 1.0, 0.5,
                null, null, null, null
        );
        Token left = new Token("4239765244", element);
        Token right = new Token("14239765244", element);

        double score = SimilarityMatchFunction.phoneNumber().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetPhoneNumber_Fail() {
        Element element = new Element(ElementType.PHONE, null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("4239765244", element);
        Token right = new Token("4239765243", element);

        double score = SimilarityMatchFunction.phoneNumber().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }

    @Test
    public void itShouldGetEquality_Success() {
        Element element = new Element(ElementType.EMAIL, null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("cat", element);
        Token right = new Token("CAT", element);

        double score = SimilarityMatchFunction.equality().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetEquality_Fail() {
        Element element = new Element(ElementType.EMAIL, null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("cat", element);
        Token right = new Token("bat", element);

        double score = SimilarityMatchFunction.equality().apply(left, right);
        Assert.assertEquals(0.0, score, 0.0);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWith1() {
        Element element = new Element(ElementType.NUMBER,null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("12.23", element);
        Token right = new Token("12.23", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(1.0, score, 0.0);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWithLessThan1() {
        Element element = new Element(ElementType.NUMBER,null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("80", element);
        Token right = new Token("100", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(0.778, score, 0.001);
    }

    @Test
    public void itShouldGetNumberDifferenceRateWith0() {
        Element element = new Element(ElementType.NUMBER,null,"", 1.0, 0.5,
                null, null, null, null);
        Token left = new Token("1000", element);
        Token right = new Token("80", element);

        double score = SimilarityMatchFunction.numberDifferenceRate().apply(left, right);
        Assert.assertEquals(0.0, score, 0.001);
    }

}
