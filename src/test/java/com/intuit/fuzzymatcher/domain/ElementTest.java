package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.function.TokenizerFunction;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intuit.fuzzymatcher.domain.ElementType.NAME;

public class ElementTest {

    MatchService matchService = new MatchService();

    @Test
    public void itShouldSetTokenizerFunction() {
        List<String> names = Arrays.asList("Brian Wilson", "Bryan Wilkson");

        // Test with Default using Soundex Tokenizer
        List<Document> documents1 = getDocuments(names, null);

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(documents1);
        Assert.assertEquals(2, result1.size());
        Assert.assertEquals(1.0, result1.get(documents1.get(0)).get(0).getResult(), .01);

        // Test with override
        List<Document> documents2 = getDocuments(names, TokenizerFunction.wordTokenizer());

        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(documents2);
        Assert.assertEquals(0, result2.size());

    }

    @Test
    public void itShouldNotMatchPhoneticWordsWithChainTokenizerFunction() {
        List<String> names = Arrays.asList("bold", "bolt");

        List<Document> documents1 = getDocuments(names, TokenizerFunction.wordSoundexEncodeTokenizer());

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(documents1);
        Assert.assertEquals(2, result1.size());
        Assert.assertEquals(1.0, result1.get(documents1.get(0)).get(0).getResult(), .01);

        List<Document> documents2 = getDocuments(names,
                TokenizerFunction.chainTokenizers(TokenizerFunction.wordTokenizer(),
                        TokenizerFunction.wordSoundexEncodeTokenizer(), TokenizerFunction.triGramTokenizer()));

        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(documents2);
        Assert.assertEquals(0, result2.size());
    }
    
    @Test
    public void itShouldNotMatchPhoneticWordsWithChainTokenizerFunction2() {
        List<String> names = Arrays.asList("Caputo", "Chabot");

        List<Document> documents1 = getDocuments(names, TokenizerFunction.wordSoundexEncodeTokenizer());

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(documents1);
        Assert.assertEquals(2, result1.size());
        Assert.assertEquals(1.0, result1.get(documents1.get(0)).get(0).getResult(), .01);

        List<Document> documents2 = getDocuments(names, TokenizerFunction.chainTokenizers(TokenizerFunction.wordSoundexEncodeTokenizer (), TokenizerFunction.triGramTokenizer()));

        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(documents2);
        Assert.assertEquals(0, result2.size());
    }

    @Test
    public void itShouldMatchUnequalWordsWithChainTokenizerFunction() {
        List<String> names = Arrays.asList("Mario", "Marieo");

        List<Document> documents1 = getDocuments(names, TokenizerFunction.wordTokenizer());

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(documents1);
        Assert.assertEquals(0, result1.size());

        List<Document> documents2 = getDocuments(names, TokenizerFunction
                .chainTokenizers(TokenizerFunction.wordSoundexEncodeTokenizer(), TokenizerFunction.triGramTokenizer()));

        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(documents2);
        Assert.assertEquals(2, result2.size());
        Assert.assertEquals(0.6, result2.get(documents1.get(0)).get(0).getResult(), .01);
    }
    
    @Test
    public void itShouldMatchUnequalWordsWithChainTokenizerFunction2() {
        List<String> names = Arrays.asList("Nikolau", "Nikolaou");

        List<Document> documents1 = getDocuments(names, TokenizerFunction.wordTokenizer());

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(documents1);
        Assert.assertEquals(0, result1.size());

        List<Document> documents2 = getDocuments(names, TokenizerFunction.chainTokenizers(TokenizerFunction.wordTokenizer(), TokenizerFunction.triGramTokenizer()));

        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(documents2);
        Assert.assertEquals(2, result2.size());
        Assert.assertEquals(0.58, result2.get(documents1.get(0)).get(0).getResult(), .01);
    }

    private List<Document> getDocuments(List<String> names, Function tokenizerFunction) {
        AtomicInteger counter = new AtomicInteger();
        return names.stream().map(name -> {
            Element.Builder<String> elementBuilder = new Element.Builder<String>().setType(NAME).setValue(name);
            if(tokenizerFunction != null) {
                elementBuilder.setTokenizerFunction(tokenizerFunction);
            }
            Document document = new Document.Builder(""+ counter.incrementAndGet())
                    .addElement(elementBuilder.createElement())
                    .createDocument();
            return document;
        }).collect(Collectors.toList());
    }



}
