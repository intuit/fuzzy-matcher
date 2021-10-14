package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intuit.fuzzymatcher.domain.ElementType.ADDRESS;
import static com.intuit.fuzzymatcher.domain.ElementType.NAME;
import static com.intuit.fuzzymatcher.domain.ElementType.PHONE;

public class ElementMatchTest {

    private ElementMatch elementMatch = new ElementMatch();
    AtomicInteger atomicInteger = new AtomicInteger();

    @Test
    public void itShouldNotScoreMoreThanOneForName() {
        Element<String> element1 = getElement(NAME,"Rodrigue Rodrigues");
        Element<String> element2 = getElement(NAME,"Rodrigues, Rodrigue");

        Set<Match<Element>> matchSet1 = elementMatch.matchElement(element1);
        Assert.assertEquals(0, matchSet1.size());

        Set<Match<Element>> matchSet2 = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet2.size());
        Assert.assertEquals(1.0, matchSet2.iterator().next().getResult(), 0.0);
    }


    @Test
    public void itShouldNotScoreMoreThanOneForAddress() {
        Element<String> element1 = getElement(ADDRESS,"325 NS 3rd Street Ste 567 Miami FL 33192");
        Element<String> element2 = getElement(ADDRESS,"325 NS 3rd Street Ste 567 Miami FL 33192");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet.size());
        Assert.assertEquals(1.0, matchSet.iterator().next().getResult(), 0.0);
    }

    @Test
    public void itShouldGiveAverageScoreWithBalancedElements(){
        // 2 match strings out of 5 max - score 0.4
        Element element1 = getElement(ADDRESS, "54th Street 546th avenue florida ");
        Element element2 = getElement(ADDRESS, "95th Street 765th avenue Texas");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet.size());
        Assert.assertEquals(0.4, matchSet.iterator().next().getResult(), 0.0);

    }

    @Test
    public void itShouldGiveAverageScoreWithUnbalancedElements() {
        // 3 out of 5 - Score 0.6
        Element element1 = getElement(ADDRESS, "123 new st. ");
        Element element2 = getElement(ADDRESS, "123 new street. Minneapolis MN");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet.size());
        Assert.assertEquals(0.6, matchSet.iterator().next().getResult(), 0.0);

    }


    @Test
    public void itShouldNotMatch() {
        Element element1 = getElement(ADDRESS, "456 college raod, Ohio");
        Element element2 = getElement(ADDRESS, "123 new street. Minneapolis MN");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);

        Assert.assertEquals(0, matchSet.size());
    }


    @Test
    public void itShouldMatchElementsWithRepeatingTokens() {
        Element element1 = getElement(ADDRESS, "123 new Street new street");
        Element element2 = getElement(ADDRESS, "123 new Street");

        Set<Match<Element>> matchSet1 =  elementMatch.matchElement(element1);
        Assert.assertEquals(0, matchSet1.size());

        Set<Match<Element>> matchSet2  = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet2.size());
        Assert.assertEquals(1.0, matchSet2.iterator().next().getResult(), 0.0);
        Assert.assertTrue(matchSet2.iterator().next().getData().equals(element2));
        Assert.assertTrue(matchSet2.iterator().next().getMatchedWith().equals(element1));
    }

    @Test
    public void itShouldMatchUnorderedTokens() {
        Element element1 = getElement(ADDRESS, "James Parker");
        Element element2 = getElement(ADDRESS, "Parker Jamies");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet.size());
        Assert.assertEquals(1.0, matchSet.iterator().next().getResult(), 0.0);
    }

    @Test
    public void itShouldMatchPhoneTokens() {
        Element element1 = getElement(PHONE, "+1 (123) 234-2345");
        Element element2 = getElement(PHONE, "123-234-2345");

        elementMatch.matchElement(element1);
        Set<Match<Element>> matchSet = elementMatch.matchElement(element2);
        Assert.assertEquals(1, matchSet.size());
        Assert.assertEquals(1.0, matchSet.iterator().next().getResult(), 0.0);
    }

    private Element getElement(ElementType elementType, String value) {
        Element<String> element = new Element.Builder().setType(elementType)
                .setValue(value).createElement();
        new Document.Builder(atomicInteger.incrementAndGet()+"").addElement(element).createDocument();
        return element;
    }

}
