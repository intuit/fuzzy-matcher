package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.component.Dictionary;
import com.intuit.fuzzymatcher.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A functional interface to pre-process the elements. These function are applied to element.value String's
 */

public class PreProcessFunction<T>{

    /**
     * Uses Apache commons StringUtils trim method
     *
     * @return the function to perform trim
     */
    public static Function<String, String> trim() {
        return (str) -> StringUtils.trim(str);
    }

    /**
     * Uses Apache commons StringUtils lowerCase method
     *
     * @return the function to perform toLowerCase
     */
    public static Function<String, String> toLowerCase() {
        return (str) -> StringUtils.lowerCase(str);
    }

    /**
     * replaces all non-numeric characters in a string
     *
     * @return the function to perform numericValue
     */
    public static Function<String, String> numericValue() {
        return (str) -> str.replaceAll("[^0-9]", "");
    }

    /**
     * removes special characters in a string
     *
     * @return the function to perform removeSpecialChars
     */
    public static Function<String, String> removeSpecialChars() {
        return (str) -> str.replaceAll("[^A-Za-z0-9 ]+", "");
    }

    /**
     * Used for emails, remove everything after the '@' character
     *
     * @return the function to perform removeDomain
     */
    public static Function<String, String> removeDomain() {
        return str -> {
            if (StringUtils.contains(str, "@")) {
                int index = str.indexOf('@');
                return str.substring(0, index);
            }
            return str;
        };
    }

    /**
     * applies both "RemoveSpecialChars" and also "addressNormalization" functions
     *
     * @return the function to perform addressPreprocessing
     */
    public static Function<String, String> addressPreprocessing() {
        return (str) -> removeSpecialChars().andThen(addressNormalization()).apply(str);
    }

    /**
     * applies "removeTrailingNumber", "removeSpecialChars" and "nameNormalization" functions
     *
     * @return the function to perform namePreprocessing
     */
    public static Function<String, String> namePreprocessing() {
        return (str) -> removeTrailingNumber().andThen(removeSpecialChars()).andThen(nameNormalization()).apply(str);
    }

    /**
     * Uses "address-dictionary" to normalize commonly uses string in addresses
     * eg. "st.", "street", "ave", "avenue"
     *
     * @return the function to perform addressNormalization
     */
    public static Function<String, String> addressNormalization() {
        return str -> Utils.getNormalizedString(str, Dictionary.addressDictionary);
    }

    /**
     * Removes numeric character from the end of a string
     *
     * @return the function to perform removeTrailingNumber
     */
    public static Function<String, String> removeTrailingNumber() {
        return (str) -> str.replaceAll("\\d+$", "");
    }

    /**
     * Uses "name-dictionary" to remove common prefix and suffix in user names. like "jr", "sr", etc
     * It also removes commonly used words in company names "corp", "inc", etc
     *
     * @return the function to perform nameNormalization
     */
    public static Function<String, String> nameNormalization() {
        return str -> Utils.getNormalizedString(str, Dictionary.nameDictionary);
    }

    /**
     * Returns a 10 character string, removes country code if present.
     *
     * @return the function to perform phoneNormalization
     */
    public static Function<String, String> phoneNormalization() {
        return str -> numericValue().andThen(s -> (s.length() > 10) ? s.substring(s.length()-10) : s).apply(str);
    }

    /**
     * removes all characters and retains only double numbers
     *
     * @return PreProcessFunction
     */
    public static Function numberPreprocessing() {
        return (obj) ->  {
            if (obj instanceof String) {
                String str = obj.toString();
                Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher matcher = pattern.matcher(str);
                return matcher.find() ? matcher.group() : str;
            } else {
                return none().apply(obj);
            }

        };
    }

    /**
     * Does nothing, used for already preprocessed values
     *
     * @return PreProcessFunction
     */
    public static Function none() {
        return obj -> obj;
    }
}
