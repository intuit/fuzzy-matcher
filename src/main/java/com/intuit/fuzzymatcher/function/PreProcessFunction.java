package com.intuit.fuzzymatcher.function;

import com.intuit.fuzzymatcher.component.Dictionary;
import com.intuit.fuzzymatcher.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A functional interface to pre-process the elements. These function are applied to element.value String's
 */
public interface PreProcessFunction extends Function<Object, Object> {

    /**
     * Uses Apache commons StringUtils trim method
     *
     * @return the function to perform trim
     */
    static PreProcessFunction trim() {
        return (obj) -> StringUtils.trim(obj.toString());
    }

    /**
     * Uses Apache commons StringUtils lowerCase method
     *
     * @return the function to perform toLowerCase
     */
    static PreProcessFunction toLowerCase() {
        return (obj) -> StringUtils.lowerCase(obj.toString());
    }

    /**
     * replaces all non-numeric characters in a string
     *
     * @return the function to perform numericValue
     */
    static PreProcessFunction numericValue() {
        return (obj) -> obj.toString().replaceAll("[^0-9]", "");
    }

    /**
     * removes special characters in a string
     *
     * @return the function to perform removeSpecialChars
     */
    static PreProcessFunction removeSpecialChars() {
        return (obj) -> obj.toString().replaceAll("[^A-Za-z0-9 ]+", "");
    }

    /**
     * Used for emails, remove everything after the '@' character
     *
     * @return the function to perform removeDomain
     */
    static PreProcessFunction removeDomain() {
        return obj -> {
            String str = obj.toString();
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
    static PreProcessFunction addressPreprocessing() {
        return (obj) -> removeSpecialChars().andThen(addressNormalization()).apply(obj);
    }

    /**
     * applies "removeTrailingNumber", "removeSpecialChars" and "nameNormalization" functions
     *
     * @return the function to perform namePreprocessing
     */
    static PreProcessFunction namePreprocessing() {
        return (obj) -> removeTrailingNumber().andThen(removeSpecialChars()).andThen(nameNormalization()).apply(obj);
    }

    /**
     * Uses "address-dictionary" to normalize commonly uses string in addresses
     * eg. "st.", "street", "ave", "avenue"
     *
     * @return the function to perform addressNormalization
     */
    static PreProcessFunction addressNormalization() {
        return obj -> Utils.getNormalizedString(obj.toString(), Dictionary.addressDictionary);
    }

    /**
     * Removes numeric character from the end of a string
     *
     * @return the function to perform removeTrailingNumber
     */
    static PreProcessFunction removeTrailingNumber() {
        return (obj) -> obj.toString().replaceAll("\\d+$", "");
    }

    /**
     * Uses "name-dictionary" to remove common prefix and suffix in user names. like "jr", "sr", etc
     * It also removes commonly used words in company names "corp", "inc", etc
     *
     * @return the function to perform nameNormalization
     */
    static PreProcessFunction nameNormalization() {
        return obj -> Utils.getNormalizedString(obj.toString(), Dictionary.nameDictionary);
    }

    /**
     * For a 10 character string, it prefixes it with US international code of "1".
     *
     * @return the function to perform usPhoneNormalization
     */
    static PreProcessFunction usPhoneNormalization() {
        return obj -> numericValue().andThen(s -> (s.toString().length() == 10) ? "1" + s : s).apply(obj);
    }

    /**
     * removes all characters and retains only double numbers
     *
     * @return
     */
    static PreProcessFunction numberPreprocessing() {
        return (obj) ->  {
            if (obj instanceof Double) {
                return obj;
            }
            String str = obj.toString();
            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            Matcher matcher = pattern.matcher(str);
            return matcher.find() ? matcher.group() : str;
        };
    }

    /**
     * Does nothing, used for already preprocessed values
     *
     * @return
     */
    static PreProcessFunction none() {
        return obj -> obj;
    }
}
