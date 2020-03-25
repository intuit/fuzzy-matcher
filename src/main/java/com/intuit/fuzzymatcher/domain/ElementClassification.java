package com.intuit.fuzzymatcher.domain;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Defines how each element is classified using ElementType and variance.
 * <ul>
 * <li>ElementType is an enum which gives a template on all the functions that should be applied during match</li>
 * <li>Variance is a user defined String, that allows multiple ElementType to be defined in a Document</li>
 * </ul>
 */
public class ElementClassification {

    private ElementType elementType;

    private String variance;

    public ElementClassification(ElementType elementType, String variance) {
        this.elementType = elementType;
        this.variance = variance;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public String getVariance() {
        return variance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementClassification that = (ElementClassification) o;
        return elementType == that.elementType &&
                Objects.equals(variance, that.variance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType, variance);
    }
}
