package com.intuit.fuzzymatcher.function;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.function.BiFunction;

/**
 * A function interface for Match Algorithms
 */
public interface SimilarityMatchFunction extends BiFunction<Token, Token, Double> {

    static SimilarityMatchFunction levenshtein() {
        return (left, right) -> {
            double maxLength = (left.getValue().length() > right.getValue().length()) ? left.getValue().length() : right.getValue().length();
            return (maxLength - StringUtils.getLevenshteinDistance(left.getValue().toLowerCase(), right.getValue().toLowerCase())) / maxLength;
        };
    }

    static SimilarityMatchFunction jaccard() {
        return (left, right) -> {
            JaccardSimilarity jc = new JaccardSimilarity();
            return jc.apply(left.getValue().toLowerCase(), right.getValue().toLowerCase());
        };
    }

    static SimilarityMatchFunction jarowinkler() {
        return (left, right) -> {
            JaroWinklerDistance distance = new JaroWinklerDistance();
            return distance.apply(left.getValue().toLowerCase(), right.getValue().toLowerCase());
        };
    }

    static SimilarityMatchFunction equality() {
        return (left, right) -> left.getValue().equalsIgnoreCase(right.getValue()) ? 1.0 : 0.0;
    }


    static SimilarityMatchFunction soundex() {
        return (left, right) -> {
            Soundex soundex = new Soundex();
            if (StringUtils.isNumeric(left.getValue()) || StringUtils.isNumeric(right.getValue())) {
                return left.getValue().equalsIgnoreCase(right.getValue()) ? 1.0 : 0.0;
            } else {
                String leftEncode = soundex.encode(left.getValue());
                String rightEncode = soundex.encode(right.getValue());
                return leftEncode.equals(rightEncode) ? 1.0 : 0.0;
            }
        };
    }

    //Get Phone Number Matched
    static SimilarityMatchFunction phoneNumber() {
        return (left, right) -> {
            PhoneNumberUtil.MatchType output = PhoneNumberUtil.getInstance().isNumberMatch(left.getValue(), right.getValue());
            return (output == PhoneNumberUtil.MatchType.NSN_MATCH
                    || output == PhoneNumberUtil.MatchType.EXACT_MATCH
                    || output == PhoneNumberUtil.MatchType.SHORT_NSN_MATCH) ? 1.0 : 0.0;
        };
    }
}
