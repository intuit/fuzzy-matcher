package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.AppConfig;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.intuit.fuzzymatcher.domain.ElementType.*;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class MatchServicePerfTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchServiceTest.class);

    @InjectMocks
    @Autowired
    private MatchService matchService;

    @Test
    public void itShouldApplyMatchForBigData() throws IOException {
        List<Document> bigData = getBigDataDocuments();
        long startTime = System.nanoTime();
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(bigData);
        long endTime = System.nanoTime();
//        result.entrySet().forEach(entry -> {
//            entry.getValue().forEach(match -> {
//                System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
//            });
//        });
        Assert.assertEquals(116, result.size());
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Execution time (ms): " + duration);
    }

    public List<Document> getBigDataDocuments() throws FileNotFoundException {
        AtomicInteger index = new AtomicInteger();
        return StreamSupport.stream(MatchServiceTest.getCSVReader("Sample-Big-Data.csv").spliterator(), false).map(csv -> {
            return new Document.Builder(index.incrementAndGet() + "")
                    .addElement(new Element.Builder().setType(NAME).setValue(csv[0]).createElement())
                    .addElement(new Element.Builder().setType(ADDRESS).setValue(getAddress(csv)).createElement())
                    .addElement(new Element.Builder().setType(PHONE).setValue(csv[5]).createElement())
                    .addElement(new Element.Builder().setType(EMAIL).setValue(csv[6]).createElement())
                    .createDocument();
        }).collect(Collectors.toList());
    }

    static String getAddress(String[] csv) {
        String address= StringUtils.EMPTY;
        if(csv!=null){
            StringJoiner addressBuilder = new StringJoiner(" ");
            addressBuilder.add(csv[1]);
            addressBuilder.add(csv[2]);
            addressBuilder.add(csv[3]);
            addressBuilder.add(csv[4]);
            address =  StringUtils.trimToEmpty(addressBuilder.toString());
        }
        return address;
    }
}
