package com.intuit.fuzzymatcher.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.intuit.fuzzymatcher.domain.ElementType.DATE;
import static com.intuit.fuzzymatcher.domain.ElementType.NUMBER;

public class TokenTest {

    @Test
    public void shouldCompareNumericallyForNumericalData() {
        Element element = new Element.Builder().setType(NUMBER).setValue("default").createElement();
        Token t_5 = new Token(5, element);
        Token t_1 = new Token(1, element);
        Token t_3 = new Token(3, element);
        Token t_2 = new Token(2, element);
        Token t_4 = new Token(4, element);
        Token t_null = null;

        List<Token> tokenList = Arrays.asList(t_5, t_1, t_null, t_3, t_2, t_4);
        Collections.sort(tokenList, Token.byValue);

        tokenList.forEach(System.out::println);
    }

    @Test
    public void shouldCompareNumericallyForStringlData() {
        Element element = new Element.Builder().setType(NUMBER).setValue("default").createElement();
        Token t_5 = new Token("Eva", element);
        Token t_1 = new Token("Amber", element);
        Token t_3 = new Token("Carl", element);
        Token t_2 = new Token("Betty", element);
        Token t_4 = new Token("Dory", element);
        Token t_null = null;

        List<Token> tokenList = Arrays.asList(t_5, t_1, t_null, t_3, t_2, t_4);
        Collections.sort(tokenList, Token.byValue);

        tokenList.forEach(System.out::println);
    }

    @Test
    public void shouldCompareNumericallyForDate() {
        Element element = new Element.Builder().setType(DATE).setValue("default").createElement();
        Token t_5 = new Token(LocalDateTime.now().plusDays(5), element);
        Token t_1 = new Token(LocalDateTime.now().plusDays(1), element);
        Token t_3 = new Token(LocalDateTime.now().plusDays(3), element);
        Token t_2 = new Token(LocalDateTime.now().plusDays(2), element);
        Token t_4 = new Token(LocalDateTime.now().plusDays(4), element);
        Token t_null = null;

        List<Token> tokenList = Arrays.asList(t_5, t_1, t_null, t_3, t_2, t_4);
        Collections.sort(tokenList, Token.byValue);

        tokenList.forEach(System.out::println);
    }
}
