package com.intuit.fuzzymatcher.domain;

/**
 *
 * This class is used for optimization.
 * This helps reduce the n-squared complexity of matching each token for a given type with every other element.
 * Tokens are broken down to NGram and are used to identify which set of Token's should be matched.
 * A similar set of Token's are identified and stored in the Tokens as "SearchGroups"
 */
public class NGram extends Token {
    public NGram(String value, Token token) {
        super(value, token.getElement());
        this.token = token;
    }

    private Token token;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NGram nGram = (NGram) o;

        if (!token.equals(nGram.token)) return false;
        return this.getValue().equals(nGram.getValue());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + this.getValue().hashCode();
        return result;
    }
}
