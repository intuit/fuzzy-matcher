package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.AppConfig;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.function.PreProcessFunction;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.intuit.fuzzymatcher.domain.ElementType.*;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class MatchServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchServiceTest.class);

    @InjectMocks
    @Autowired
    private MatchService matchService;

    @Test
    public void itShouldApplyMatchForDemo() throws IOException {
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(getDemoDocuments());
        result.entrySet().forEach(entry -> {
            entry.getValue().forEach(match -> {
                System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
            });
        });
        Assert.assertEquals(5, result.size());
    }

    @Test
    public void itShouldApplyMatchByDocIdForSingleDoc() throws IOException {
        Document doc = new Document.Builder("TestMatch")
                .addElement(new Element.Builder().setType(NAME).setValue("john doe").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("546 freeman ave dallas tx 75024").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("2122232235").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("john@doe.com").createElement())
                .createDocument();
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(doc, getTestDocuments());
        writeOutput(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void itShouldApplyMatchByDocIdForAList() throws IOException {
        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(getTestDocuments());
        writeOutput(result);
        Assert.assertEquals(6, result.size());
    }

    public static Stream<Element> getOrderedElements(Set<Element> elements) {
        List<Element> l = elements.stream().sorted(Comparator.comparing(Element::getType)).collect(Collectors.toList());
        return l.stream();
    }

    @Test
    public void itShouldApplyMatchForMultiplePhoneNumber() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("Kapa Limited").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("texas").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("8204354957 xyz").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("(848) 398-3868").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@kapalimited.com").createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("Tram Kapa Ltd LLC").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("texas").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("(848) 398-3868").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("(820) 435-4957").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@nekoproductions.com").createElement())
                .createDocument());
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertEquals(2, result.size());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
    }

    @Test
    public void itShouldApplyMatchForMultipleEmptyPhoneNumber() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("Kapa Limited").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("texas").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("8204354957 xyz").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("(848) 398-3868").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@kapalimited.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("Tram Kapa Ltd LLC").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("texas").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@nekoproductions.com").setThreshold(0.5).createElement())
                .createDocument());
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertEquals(2, result.size());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
    }

    @Test
    public void itShouldApplyMatchForDuplicateTokensWithNoMatch() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("lucky DAVID ABC").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 W Plano St PLANO TX 33130").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("Ramirez Yara").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 W Plano St 2111 Plano TX 33130").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("1231231234").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("yara1345@gmail.com").setThreshold(0.5).createElement())
                .createDocument());
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void itShouldApplyMatch() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new Street, minneapolis mn").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("123-234-2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james_parker@domain.com").setThreshold(0.5).createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertEquals(2, result.size());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
    }


    @Test
    public void itShouldApplyMatchWith3Documents() throws FileNotFoundException {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new Street, minneapolis mn").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("123-234-2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james_parker@domain.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("3")
                .addElement(new Element.Builder().setType(NAME).setValue("John D").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("33 hammons Dr. Texas").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("9901238484").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("d_john@domain.com").setThreshold(0.5).createElement())
                .createDocument());
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        int totalMatches = result.values().stream().mapToInt(List::size).sum();
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
        Assert.assertEquals(2, totalMatches);
    }

    @Test
    public void itShouldApplyMatchWithFailure() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("Peter Watson").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("321 john Q Hammons Street, Plano, TX - 75054").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("9091238877").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("peter.watson@domain.com").setThreshold(0.5).createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void itShouldApplyMatchForMulitipleEmptyField() throws FileNotFoundException {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new street Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("").setThreshold(0.5).createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void itShouldApplyMatchForEmptyInput() {
        List<Document> inputData = new ArrayList<>();
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void itShouldApplyMatchForWhiteSpaceWithNoFalsePositive() {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("sdwet ert rdfgh, LLC").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue(" ").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("sdwet@abc.com").setThreshold(0.5).createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("sad sdf LLC").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue(" ").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("sad@something.com").setThreshold(0.5).createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertTrue(result.isEmpty());
    }

    //It tests whether there is any match between two different type element
    @Test
    public void itShouldApplyMatchElementsWithDifferentType() {
        List<Document> documents = new ArrayList<>();
        documents.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("John d").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("freeman ave dallas 75024").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("435-221-5432").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("john_doe@gmail.com").createElement())
                .createDocument());
        documents.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("john doe").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("546 freeman avenue dallas tx 75024").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("435-334-2234").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("john@doe.com").createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(documents);
        Assert.assertTrue(result.isEmpty());
    }

    public List<Document> getTestDocuments() throws FileNotFoundException {
        return StreamSupport.stream(getCSVReader("test-data.csv").spliterator(), false).map(csv -> {
            return new Document.Builder(csv[0])
                    .addElement(new Element.Builder().setType(NAME).setValue(csv[1]).createElement())
                    .addElement(new Element.Builder().setType(ADDRESS).setValue(csv[2]).createElement())
                    .addElement(new Element.Builder().setType(PHONE).setValue(csv[3]).setWeight(2)
                            .setThreshold(0.5).createElement())
                    .addElement(new Element.Builder().setType(EMAIL).setValue(csv[4]).createElement())
                    .setThreshold(0.5)
                    .createDocument();
        }).collect(Collectors.toList());
    }

    public List<Document> getDemoDocuments() throws FileNotFoundException {
        AtomicInteger index = new AtomicInteger();
        return StreamSupport.stream(getCSVReader("demo.csv").spliterator(), false).map(csv -> {
            return new Document.Builder(index.incrementAndGet() + "")
                    .addElement(new Element.Builder().setType(NAME).setValue(csv[0]).createElement())
                    .createDocument();
        }).collect(Collectors.toList());
    }

    public static CSVReader getCSVReader(String FileName) throws FileNotFoundException {
        return new CSVReaderBuilder(
                new FileReader(MatchServiceTest.class.getClassLoader().getResource(FileName).getFile()))
                .withSkipLines(1)
                .build();
    }

    @Test
    public void itShouldApplyMatchWithSourceList() throws FileNotFoundException {
        List<Document> sourceData = new ArrayList<>();
        sourceData.add(new Document.Builder("S1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument());
        sourceData.add(new Document.Builder("S2")
                .addElement(new Element.Builder().setType(NAME).setValue("James").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new Street, minneapolis mn").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("123-234-2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james_parker@domain.com").setThreshold(0.5).createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(sourceData, getTestDocuments());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("S1", "S2"));
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void itShouldApplyMatchWithSourceDocument() throws FileNotFoundException {
        Document doc = new Document.Builder("S1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 new st. Minneapolis MN").createElement())
                .addElement(new Element.Builder().setType(PHONE).setWeight(2).setValue("(123) 234 2345").setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("jparker@gmail.com").setThreshold(0.5).createElement())
                .createDocument();

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(doc, getTestDocuments());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("S1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void itShouldApplyMatchWithScoreNotMoreThanOne(){
        List<Document> inputData = new ArrayList<>();
        Document doc1 = new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("Kapa Limited").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 some street, plano, texas - 75070").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123-456-7890").setThreshold(0.5).setWeight(2).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("1234567890").setThreshold(0.5).setWeight(2).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@kapalimited.com").setThreshold(0.5).createElement())
                .createDocument();
        Document doc2 = new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("ABC CORP").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 some street, plano, texas - 75070").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123-456-7890").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("1234567890").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").setWeight(2).setThreshold(0.5).createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("kirit@nekoproductions.com").setThreshold(0.5).createElement())
                .createDocument();
        inputData.addAll(Arrays.asList(doc1,doc2));
        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertEquals(2, result.size());
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1","2"));
        Assert.assertTrue(result.get(doc1).get(0).getResult() <= 1);
    }

    @Test
    public void itShouldApplyMatchWithConfigurablePreProcessingFunctions() throws IOException {

        Map<Document, List<Match<Document>>> result1 = matchService.applyMatch(
                getTestData(PreProcessFunction.removeSpecialChars(),
                        PreProcessFunction.removeSpecialChars(), 0.7));
        Assert.assertEquals(2, result1.size());


        Map<Document, List<Match<Document>>> result2 = matchService.applyMatch(
                getTestData(PreProcessFunction.namePreprocessing(),
                        PreProcessFunction.addressPreprocessing(), 0.7));
        Assert.assertEquals(4, result2.size());
    }

    @Test
    public void itShouldApplyMatchForBalancedEmptyElements() throws FileNotFoundException {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("parker@email.com").createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james@email.com").createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
        Assert.assertEquals(0.5, result.entrySet().stream()
                .map(entry -> entry.getValue()).collect(Collectors.toList()).get(0).get(0).getResult(), 0.0);
    }

    @Test
    public void itShouldApplyMatchForUnBalancedEmptyElements() throws FileNotFoundException {
        List<Document> inputData = new ArrayList<>();
        inputData.add(new Document.Builder("1")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("123 Some Street").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("parker@email.com").createElement())
                .createDocument());
        inputData.add(new Document.Builder("2")
                .addElement(new Element.Builder().setType(NAME).setValue("James Parker").createElement())
                .addElement(new Element.Builder().setType(ADDRESS).setValue("").createElement())
                .addElement(new Element.Builder().setType(PHONE).setValue("123-123-1234").createElement())
                .addElement(new Element.Builder().setType(EMAIL).setValue("james@email.com").createElement())
                .createDocument());

        Map<Document, List<Match<Document>>> result = matchService.applyMatch(inputData);
        Assert.assertThat(result.entrySet().stream()
                        .map(entry -> entry.getKey().getKey()).collect(Collectors.toList()),
                CoreMatchers.hasItems("1", "2"));
        Assert.assertEquals(0.5, result.entrySet().stream()
                .map(entry -> entry.getValue()).collect(Collectors.toList()).get(0).get(0).getResult(), 0.0);
    }

    public static void writeOutput(Map<String, List<Match<Document>>> result) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter("src/test/resources/output.csv"));
        writer.writeNext(new String[]{"Key", "Matched Key", "Score", "Name", "Address", "Email", "Phone"});

        result.entrySet().forEach(entry -> {
            String[] keyArrs = Stream.concat(Stream.of(entry.getKey(), entry.getKey(), ""),
                    getOrderedElements(entry.getValue().stream()
                            .map(match -> match.getData())
                            .findFirst().get()
                            .getElements()).map(e -> e.getValue())).toArray(String[]::new);
            writer.writeNext(keyArrs);

            entry.getValue().stream().forEach(match -> {
                Document md = match.getMatchedWith();
                String[] matchArrs = Stream.concat(Stream.of("", md.getKey(), Double.toString(match.getResult())),
                        getOrderedElements(md.getElements()).map(e -> e.getValue())).toArray(String[]::new);
                writer.writeNext(matchArrs);
                LOGGER.info("        " + match);
            });
        });
        writer.close();
    }

    private List<Document> getTestData(Function<String, String> namePreProcessing,
                                       Function<String, String> addressPreProcessing, double docThreshold) throws FileNotFoundException {
        return StreamSupport.stream(getCSVReader("test-data.csv").spliterator(), false).map(csv -> {
            return new Document.Builder(csv[0])
                    .addElement(new Element.Builder().setType(NAME).setValue(csv[1])
                            .setPreProcessingFunction(namePreProcessing)
                            .createElement())
                    .addElement(new Element.Builder().setType(ADDRESS).setValue(csv[2])
                            .setPreProcessingFunction(addressPreProcessing)
                            .createElement())
                    .addElement(new Element.Builder().setType(PHONE).setValue(csv[3]).createElement())
                    .addElement(new Element.Builder().setType(EMAIL).setValue(csv[4]).createElement())
                    .setThreshold(docThreshold)
                    .createDocument();
        }).collect(Collectors.toList());
    }
}
