package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.intuit.fuzzymatcher.domain.ElementType.*;

/**
 *
 */
public class MatchServicePerfTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchServiceTest.class);

    private MatchService matchService = new MatchService();

    private static final int ELEM_PER_DOC = 4;

    @Test
    public void itShouldApplyMatchForBigData() throws IOException {
        applyMatch(getBigDataDocuments().limit(500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(1000).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(1500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(2000).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(2500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(3000).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(3500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(4000).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(4500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(5000).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(5500).collect(Collectors.toList()));

        applyMatch(getBigDataDocuments().limit(6000).collect(Collectors.toList()));
    }

    @Test
    /*
     * 2000 - 1.41 G
     * 4000 - 2.45 G
     * 6000 - 4.01 G
     *
     */
    public void itShouldApplyMatchForBigDataForMemoryPerf() throws FileNotFoundException {
        int docSize = 6000;
        List<Document> leftDoc = getBigDataDocuments().limit(docSize).collect(Collectors.toList());
        List<Document> rightDoc = getBigDataDocuments().limit(docSize).collect(Collectors.toList());
        recordMemoryUsage(() -> applyMatch(leftDoc, rightDoc), 10);
    }

    private void applyMatch(List<Document> documentList) {
        long startTime = System.nanoTime();
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(documentList);
        long endTime = System.nanoTime();
        //Assert.assertEquals(116, result.size());
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Execution time (ms) for + " + documentList.size() * ELEM_PER_DOC + " count : " + duration);
        System.out.println();
    }

    public Stream<Document> getBigDataDocuments() throws FileNotFoundException {
        AtomicInteger index = new AtomicInteger();
        return StreamSupport.stream(MatchServiceTest.getCSVReader("Sample-Big-Data.csv").spliterator(), false).map(csv -> {
            return new Document.Builder(index.incrementAndGet() + "")
                    .addElement(new Element.Builder().setType(NAME).setValue(csv[0]).createElement())
                    .addElement(new Element.Builder().setType(ADDRESS).setValue(getAddress(csv)).createElement())
                    .addElement(new Element.Builder().setType(PHONE).setValue(csv[5]).createElement())
                    .addElement(new Element.Builder().setType(EMAIL).setValue(csv[6]).createElement())
                    .createDocument();
        });
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

    private void applyMatch(List<Document> left, List<Document> right) {
        long startTime = System.nanoTime();
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(left, right);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Execution time (ms) for transactions : " + duration);
    }

    public void recordMemoryUsage(Runnable runnable, int runTimeSecs) {
        try {
            CompletableFuture<Void> mainProcessFuture = CompletableFuture.runAsync(runnable);
            CompletableFuture<Void> memUsageFuture = CompletableFuture.runAsync(() -> {

                long mem = 0;
                for (int cnt = 0; cnt < runTimeSecs; cnt++) {
                    long memUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    mem = memUsed > mem ? memUsed : mem;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ;
                System.out.println("Max memory used (gb): " + mem/1000000000D);
            });

            CompletableFuture<Void> allOf = CompletableFuture.allOf(mainProcessFuture, memUsageFuture);
            allOf.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
