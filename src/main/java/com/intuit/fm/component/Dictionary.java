package com.intuit.fm.component;

import com.intuit.fm.exception.MatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
@Component
public class Dictionary {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dictionary.class);
    private static Map<String, String> addressDictionary;

    private static Map<String, String> nameDictionary;

    @Value("classpath:address-dictionary.txt")
    private Resource addressDictonaryPath;

    @Value("classpath:name-dictionary.txt")
    private Resource nameDictonaryPath;

    public static Map<String, String> getAddressDictionary() {
        return addressDictionary;
    }
    public static Map<String, String> getNameDictionary() {
        return nameDictionary;
    }

    @PostConstruct
    public void setAddressDictionary() {
        try {
            addressDictionary = getDictionary(addressDictonaryPath);
        } catch (IOException e) {
            LOGGER.error("could not load address dictonary", e);
            throw new MatchException("could not load address dictonary", e);
        }
    }

    @PostConstruct
    public void setNameDictionary() {
        try {
            nameDictionary = getDictionary(nameDictonaryPath);
        } catch (IOException e) {
            LOGGER.error("could not load address dictonary", e);
            throw new MatchException("could not load address dictonary", e);
        }
    }

    private Map<String, String> getDictionary(Resource resource) throws IOException {
            return new BufferedReader(new InputStreamReader(resource.getInputStream()))
                    .lines()
                    .map(String::toLowerCase)
                    .map(s -> s.split(":", 2))
                    .collect(Collectors.toMap(arr -> arr[0].trim(), arr -> arr[1].trim(), (a1, a2) -> a1));
    }
}
