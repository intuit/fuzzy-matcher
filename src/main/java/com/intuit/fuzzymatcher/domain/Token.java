package com.intuit.fuzzymatcher.domain;

import java.util.Comparator;
import java.util.Objects;

/**
 * Elements are broken down into Token class using the TokenizerFunction
 */
public class Token<T> {

    public Token(T value, Element element) {
        this.value = value;
        this.element = element;
    }

    private T value;
    private Element element;

    public T getValue() {
        return value;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public String toString() {
        return "{" +
                value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(value, token.value) &&
                Objects.equals(element, token.element);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value, element);
    }

    public static Comparator<Token> byValue = (Token t1, Token t2) -> {
        if (t2 == null) {
            return -1;
        }
        if (t1 == null) {
            return 1;
        }

        if (t1.getValue() instanceof Comparable) {
            return ((Comparable) t1.getValue()).compareTo((Comparable) t2.getValue());
        }
        return -1;
    };
}
