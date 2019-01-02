package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.AppConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.intuit.fuzzymatcher.domain.ElementType.*;
import static com.intuit.fuzzymatcher.domain.ElementType.EMAIL;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class DocumentTest {

    @Test
    public void itShouldGetUnmatchedCountForUnbalancedDoc() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("parker@email.com").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue(" ").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123-123-1234").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james@email.com").createElement())
                .createDocument();

        Assert.assertEquals(4, d1.getChildCount(d2));
        Assert.assertEquals(4, d2.getChildCount(d1));
        Assert.assertEquals(2, d1.getUnmatchedChildCount(d2));
    }

    @Test
    public void itShouldGetUnmatchedCountForBalancedDoc() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();

        Assert.assertEquals(2, d1.getChildCount(d2));
        Assert.assertEquals(0, d1.getUnmatchedChildCount(d2));
    }

    @Test
    public void itShouldGetUnmatchedCountForUnbalancedDocWithEmpty() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();

        Assert.assertEquals(3, d1.getChildCount(d2));
        Assert.assertEquals(2, d1.getUnmatchedChildCount(d2));
    }

    @Test
    public void itShouldGetUnmatchedCountForMultiElementTypes() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("234").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();

        Assert.assertEquals(4, d1.getChildCount(d2));
        Assert.assertEquals(3, d1.getUnmatchedChildCount(d2));
    }

    @Test
    public void itShouldGetUnmatchedCountForMultiElementTypesWithNonMatch() {
        Document d1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();
        Document d2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("567").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").createElement())
                .createDocument();

        Assert.assertEquals(3, d1.getChildCount(d2));
        Assert.assertEquals(1, d1.getUnmatchedChildCount(d2));
    }
}
