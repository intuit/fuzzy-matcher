package com.intuit.fuzzymatcher.function;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.intuit.fuzzymatcher.domain.Token;
import com.intuit.fuzzymatcher.exception.MatchException;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * A function interface for Match Algorithms
 */
public interface SimilarityMatchFunction extends BiFunction<Token, Token, Double> {

    static SimilarityMatchFunction levenshtein() {
        return (leftToken, rightToken) -> {
            String left = leftToken.getValue().toString();
            String right = rightToken.getValue().toString();
            double maxLength = (left.length() > right.length()) ? left.length() : right.length();
            return (maxLength - StringUtils.getLevenshteinDistance(left.toLowerCase(), right.toLowerCase())) / maxLength;
        };
    }

    static SimilarityMatchFunction jaccard() {
        return (left, right) -> {
            JaccardSimilarity jc = new JaccardSimilarity();
            return jc.apply(left.getValue().toString().toLowerCase(), right.getValue().toString().toLowerCase());
        };
    }

    static SimilarityMatchFunction jarowinkler() {
        return (left, right) -> {
            JaroWinklerDistance distance = new JaroWinklerDistance();
            return distance.apply(left.getValue().toString().toLowerCase(), right.getValue().toString().toLowerCase());
        };
    }

    static SimilarityMatchFunction equality() {
        return (left, right) -> {
            if (left.getValue() instanceof String && right.getValue() instanceof String) {
                return (left.getValue().toString()).equalsIgnoreCase(right.getValue().toString()) ? 1.0 : 0.0;
            }
            return left.getValue().equals(right.getValue()) ? 1.0 : 0.0;
        };
    }


    static SimilarityMatchFunction soundex() {
        return (leftToken, rightToken) -> {
            String left = leftToken.getValue().toString();
            String right = rightToken.getValue().toString();
            Soundex soundex = new Soundex();
            if (StringUtils.isNumeric(left) || StringUtils.isNumeric(right)) {
                return left.equalsIgnoreCase(right) ? 1.0 : 0.0;
            } else {
                String leftEncode = soundex.encode(left);
                String rightEncode = soundex.encode(right);
                return leftEncode.equals(rightEncode) ? 1.0 : 0.0;
            }
        };
    }

    //Get Phone Number Matched
    static SimilarityMatchFunction phoneNumber() {
        return (left, right) -> {
            PhoneNumberUtil.MatchType output = PhoneNumberUtil.getInstance()
                    .isNumberMatch(left.getValue().toString(), right.getValue().toString());
            return (output == PhoneNumberUtil.MatchType.NSN_MATCH
                    || output == PhoneNumberUtil.MatchType.EXACT_MATCH
                    || output == PhoneNumberUtil.MatchType.SHORT_NSN_MATCH) ? 1.0 : 0.0;
        };
    }

    static SimilarityMatchFunction numberDifferenceRate() {
        return (left, right) -> {
            if (left.getValue() instanceof Double && right.getValue() instanceof Double) {
                return getNumberDifferenceRate((Double) left.getValue(), (Double) right.getValue());
            }
            Double leftValue = NumberUtils.isNumber(left.getValue().toString()) ? Double.valueOf(left.getValue().toString()) : Double.NaN;
            Double rightValue = NumberUtils.isNumber(right.getValue().toString()) ? Double.valueOf(right.getValue().toString()) : Double.NaN;
            if(leftValue.isNaN()|| rightValue.isNaN()) {
                return  0.0;
            }
            return getNumberDifferenceRate(leftValue, rightValue);
        };
    }

    static double getNumberDifferenceRate(double left, double right) {
        Double diff = Math.abs(left - right);
        Double denominator = (left + right) / 2;
        Double result = 1 - diff / denominator;
        return ensureRange(result, 0.0, 1.0);
    }

    static double ensureRange(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    static SimilarityMatchFunction dateDifferenceWithinYear() {
        return (left, right) -> {
            if (!(left.getValue() instanceof Date && right.getValue() instanceof Date)) {
                throw new MatchException("input values are not Dates");
            }
            Date leftValue = (Date) left.getValue();
            Date rightValue = (Date) right.getValue();
            long diffInMillies = Math.abs(leftValue.getTime() - rightValue.getTime());
            double diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            double result = 1D - (diff/365);
            return ensureRange(result, 0.0, 1.0);
        };
    }
}
