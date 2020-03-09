package com.intuit.fuzzymatcher.domain;

import com.intuit.fuzzymatcher.function.ScoringFunction;
import com.intuit.fuzzymatcher.util.Utils;
import org.apache.commons.codec.language.Soundex;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Elements are broken down into Token class using the TokenizerFunction
 */
public class Token implements Matchable {

    public Token(Object value, Element element) {
        this(value, element, false);
    }

    public Token(Object value, Element element, boolean nGramTokenized) {
        this.value = value;
        this.element = element;
        this.nGramTokenized = nGramTokenized;
    }

    private Object value;
    private Element element;
    private boolean nGramTokenized;
    private Stream<Token> searchGroups;
    private Object encodedValue;

    public Object getValue() {
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
        if (isnGramTokenized()) {
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

    public Object getEncodedValue() {
        if (this.encodedValue != null) {
            return this.encodedValue;
        }
        MatchType matchType = getElement().getElementClassification().getElementType().getMatchType();
        switch (matchType) {
            case SOUNDEX:
                try {
                    String val = (String) getValue();
                    if (isNumeric(val)) {
                        this.encodedValue = val;

                    } else {
                        Soundex soundex = new Soundex();
                        this.encodedValue = soundex.encode(val);
                        if (this.encodedValue.equals("")) {
                            this.encodedValue = val;

                        }
                    }
                } catch (Exception ee) {
                    this.encodedValue = getValue();
                }
                break;
            default:
                this.encodedValue = getValue();
        }
        // System.out.println(getValue() + " -> " + this.encodedValue);
        return this.encodedValue;
    }

    private boolean isNumeric(String str) {
        return str.matches(".*\\d.*");
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
}
