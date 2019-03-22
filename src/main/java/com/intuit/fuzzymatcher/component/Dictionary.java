package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.exception.MatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used for Pre-Processing, the Dictionary caches a pre-defined normalization and replacement for common characters
 * found in names and adresses.
 *
 */
public class Dictionary {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dictionary.class);
    public static final Map<String, String> addressDictionary = getAddressDictionary();

    public static final Map<String, String> nameDictionary = getNameDictionary();


    private static Map<String, String>  getAddressDictionary() {
        try {
            ClassLoader classLoader = Dictionary.class.getClassLoader();
            BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("address-dictionary.txt")));
            return getDictionary(br);
        } catch (IOException e) {
            LOGGER.error("could not load address dictonary", e);
            throw new MatchException("could not load address dictonary", e);
        }
    }

    private static Map<String, String> getNameDictionary() {
        try {
            ClassLoader classLoader = Dictionary.class.getClassLoader();
            BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("name-dictionary.txt")));
            return getDictionary(br);
        } catch (IOException e) {
            LOGGER.error("could not load address dictonary", e);
            throw new MatchException("could not load address dictonary", e);
        }
    }

    private static Map<String, String> getDictionary(BufferedReader br) throws IOException {
            return br
                    .lines()
                    .map(String::toLowerCase)
                    .map(s -> s.split(":", 2))
                    .collect(Collectors.toMap(arr -> arr[0].trim(), arr -> arr[1].trim(), (a1, a2) -> a1));
    }
}
