package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.function.ScoringFunction;
import com.intuit.fuzzymatcher.util.Utils;

import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * Elements are broken down into Token class using the TokenizerFunction
 */
public class Token implements Matchable, Comparable<Token> {

    public Token(String value, Element element) {
        this(value, element, false);
    }

    public Token(String value, Element element, boolean nGramTokenized) {
        this.value = value;
        this.element = element;
        this.nGramTokenized = nGramTokenized;
    }

    private String value;
    private Element element;
    private boolean nGramTokenized;
    private Stream<Token> searchGroups;

    public String getValue() {
        return value;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean isnGramTokenized() {
        return nGramTokenized;
    }

    public Stream<NGram> getNGrams() {
        if(isnGramTokenized()){
            return Stream.of(new NGram(getValue(), this));
        } else {
            return Utils.getNGrams(getValue(), 3).map(str -> new NGram(str, this)).distinct();
        }
    }

    public Stream<Token> getSearchGroups() {
        return searchGroups == null ? Stream.empty() : searchGroups.filter(t -> t != this).distinct();
    }

    public void setSearchGroups(Stream<Token> searchGroups) {
        this.searchGroups = searchGroups;
    }

    @Override
    public long getChildCount(Matchable other) {
        return 0;
    }

    @Override
    public long getUnmatchedChildCount(Matchable other) {
        return 0;
    }

    @Override
    public ScoringFunction getScoringFunction() {
        return null;
    }

    @Override
    public double getWeight() {
        return 1.0;
    }

    @Override
    public String toString() {
        return "{" +
                value + '\'' +
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

    @Override
    public int compareTo(Token o) {
        return this.value.compareTo(o.value);
    }
}
