package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.domain.Element;
import org.junit.Assert;
import org.junit.Test;

import static com.intuit.fuzzymatcher.domain.ElementType.*;

public class PreProcessFunctionTest {
    @Test
    public void itShouldRemoveSuffixFromName_Success(){
        String value = "James Parker JR.";
        Element element = new Element.Builder().setType(NAME).setValue(value).createElement();
        Assert.assertEquals("james parker", element.getPreProcessedValue());
    }

    @Test
    public void itShouldNotRemoveNameSuffixFromAddress_Success(){
        String value = "123 XYZ Ltd st, TX";
        Element element = new Element.Builder().setType(ADDRESS).setValue(value).createElement();
        Assert.assertEquals("123 xyz ltd street texas", element.getPreProcessedValue());
    }

    @Test
    public void itShouldGetNullString_Success(){
        String value = "   ";
        Element element = new Element.Builder().setType(NAME).setValue(value).createElement();
        Assert.assertEquals("", element.getPreProcessedValue());
    }
    
    @Test
    public void itShouldRemoveTrailingNumbersFromName_Success(){
        String value = "Nova LLC-1";
        Element element = new Element.Builder().setType(NAME).setValue(value).createElement();
        Assert.assertEquals("nova", element.getPreProcessedValue());
    }

    @Test
    public void itShouldTestComposingFunction() {
        String value = "James Parker jr.";
        Element element = new Element.Builder().setType(TEXT).setValue(value).createElement();
        Assert.assertEquals("james parker jr", element.getPreProcessedValue());

        Element element1 = new Element.Builder().setType(TEXT).setValue(value)
                .setPreProcessingFunction(PreProcessFunction.removeSpecialChars()
                        .compose(str -> str.replace("jr.", ""))).createElement();
        Assert.assertEquals("james parker", element1.getPreProcessedValue());
    }

    @Test
    public void itShouldNormalizeAddress_Success(){
        String value = "123 some-street ave PLano, TX";
        Element element = new Element.Builder().setType(ADDRESS).setValue(value).createElement();
        Assert.assertEquals("123 somestreet avenue plano texas", element.getPreProcessedValue());
    }

    @Test
    public void itShouldRemoveSpecialCharPhone_Success(){
        String value = "+1-(123)-456-4345";
        Element element = new Element.Builder().setType(PHONE).setValue(value).createElement();
        Assert.assertEquals("11234564345", element.getPreProcessedValue());
    }
}