package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchOptimizerFunctionTest {

    @Test
    public void shouldApplyNumberSortOptimizer() {

        List<String> numbers = Arrays.asList("23", "22", "10", "5", "str", "9", "11", "10.5", "23.5", "str");
        AtomicInteger ai = new AtomicInteger(0);
        List<Token> tokenList = numbers.stream().map(num -> {
            Element element = getMockElement();
            when(element.getSimilarityMatchFunction()).thenReturn(SimilarityMatchFunction.numberDifferenceRate());
            when(element.getThreshold()).thenReturn(0.95);
            return new Token(num, element);
        }).collect(Collectors.toList());
       List<Match<Token>> res =  MatchOptimizerFunction.numberSortOptimizer().apply(tokenList).collect(Collectors.toList());
       Assert.assertEquals(4, res.size());
       Assert.assertEquals("10", res.get(0).getData().getValue());
       Assert.assertEquals("10.5", res.get(0).getMatchedWith().getValue());
       res.forEach(match -> Assert.assertEquals(true, match.getResult() > 0.95));
    }

    @Test
    public void shouldApplyTextSortOptimizer() {

        List<String> numbers = Arrays.asList("liz", "tom", "bill", "liz", "chil", "hill", "bill", "hill", "tom");
        AtomicInteger ai = new AtomicInteger(0);
        List<Token> tokenList = numbers.stream().map(num -> {
            Element element = getMockElement();
            when(element.getSimilarityMatchFunction()).thenReturn(SimilarityMatchFunction.equality());
            when(element.getThreshold()).thenReturn(0.5);
            return new Token(num, element);
        }).collect(Collectors.toList());
        List<Match<Token>> res =  MatchOptimizerFunction.textSortOptimizer().apply(tokenList).collect(Collectors.toList());
        Assert.assertEquals(4, res.size());
        Assert.assertEquals("bill", res.get(0).getData().getValue());
        Assert.assertEquals("bill", res.get(0).getMatchedWith().getValue());
        res.forEach(match -> Assert.assertEquals(true, match.getResult() == 1.0));
    }

    private Document getMockDocument() {
        Document doc = mock(Document.class);
        double str = Math.random();
        when(doc.getKey()).thenReturn(Double.toString(str));
        return doc;
    }

    private Element getMockElement() {
        Element element = mock(Element.class);
        Document doc = getMockDocument();
        when(element.getDocument()).thenReturn(doc);
        return element;
    }
}
