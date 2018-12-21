# <img src="fuzzy-logo.svg" height="60" width="60"/> Fuzzy-Matcher
* [Introduction](#introduction)
* [How does this work](#how-does-this-work)
    * [Four Stages of Fuzzy Match](#four-stages-of-fuzzy-match)
* [Building the Library](#building-the-library)
    * [Prerequisite](#prerequisite)
    * [Compiling and installing locally](#compiling-and-installing-locally)
* [Using the Library](#using-the-library)
    * [Maven Import](#maven-import)
    * [Input](#input)
    * [Applying the Match](#applying-the-match)
    * [Output](#output)
* [End User Configuration](#end-user-configuration)
    * [Configurable Functions](#configurable-functions)
    * [Document Configuration](#document-configuration)
    * [Element Configuration](#element-configuration)
* [Demo](#demo)

## Introduction
A java based library to match and group "similar" elements in a collection of document.

In system with a collection of contacts. If we wanted to match and categorize contacts with similar names, location
where they live or with any other attribute. This matching algorithm helps to achieve it using Fuzzy match.
In fact, you may even try to find out if you have already added duplicate contacts, or you can use this to
prevent your system from adding one.

This library can act on any domain object like contact and find similarity for various use cases.
It dives deep into each character and finds out the probability that 2 or more of these objects are similar.

## How does this work
![Fuzzy Match](fuzzy-match.png?raw=true "Fuzzy Match")

### Four Stages of Fuzzy Match
The input provided goes through 4 stages to come up with a match result. These 4 stages are functions defined which can
be easily configured by passing a lambda expression.

* __Pre-Processing__ : Expects a ```Function<String, String>``` which a simple transformation from a String to another.
    * _Trim_: Removes leading and trailing spaces (applied by default)
    * _Lower Case_: Converts all characters to lower case (applied by default)
    * _Remove Special Chars_ : Removes all characters except alphabets and numeric chars and space. (default for _TEXT_ type)
    * _Numeric_: Strips all non-numeric characters. Useful for numeric values like phone or ssn (default for _PHONE_ type)
    * _Email_: Strips away domain from an email. This prevents common domains like gmail.com, yahoo.com to be considered in match (default for _EMAIL_ type)

* __Tokenization__ : Expects a ```Function<Element, Stream<Token>>``` which will break down each Element into Token object used for comparison.
    * _Word_ : Breaks down an element into words (anything delimited by space " ").
            e.g. ```"123 some street" -> ["123" "some" "street"]```
    * _N-Gram_ : Breaks down an element into 3 letter grams.
            e.g. ```"jparker" -> ["jpa","par","ark","rke","ker"]```

* __Similarity Match__ : Expects a ```BiFunction<Token, Token, Double>```, which gives a match probability (0.0 to 1.0) between 2 tokens
    * _Soundex_: Compares 2 token strings soundex values. Uses apache commons codec library to get soundex values. The result of this match is a binary either 0.0 or 1.0
    * _Equality_: Does a string equals of 2 token strings. Again the result is either 0.0 or 1.0
    * _Levenshtein_: Gets the Levenshtein distance score using apache commons similarity library
    * _Jaccard_: Gets the Jaccard score using apache commons similarity library

* __Scoring__ : Expects a ```Function<Match, Double>```, this defines functions on how to accumulate scores from Tokens into Elements and from Elements into Documents
    * _Average_: Adds up total scores of each child matches / total children. This is the default scoring for Elements
    * _Weighted Average_: This is useful for Document Scoring, where users can input weights on elements.
        Example a phone number or email could be considered an important element to identify match between 2 User objects, and we can add weights to such elements.
    * _Exponential Average_: Again useful for Document Scoring, where if more than 1 element match, we can increase the scoring exponentially
    * _Exponential Weighted Average_: Uses both an exponents and weights for scoring. This is the default for Document Scoring

## Building the Library
### Prerequisite
You need Java SDK v1.8 or higher. Before you begin, you should check your current Java installation by using the following command:
``` java -version ```

fuzzy-match is compatible with Apache Maven 4.0 or above. If you do not already have Maven installed, you can follow the instructions at maven.apache.org.
```
On many operating systems, Maven can be installed with a package manager.
If you use OSX Homebrew, try brew install maven.
Ubuntu users can run sudo apt-get install maven.
Windows users with Chocolatey can run choco install maven from an elevated (administrator) prompt.
```
### Compiling and installing locally
After cloning the project locally. Run this command to compile, test and install the project
```
mvn clean install
```

## Using the Library

### Maven Import
The library is pusblished to maven central
```
<dependency>
    <groupId>com.intuit.fuzzymatcher</groupId>
    <artifactId>fuzzy-matcher</artifactId>
    <version>0.0.3</version>
</dependency>
```

### Input
This library take a collection of _Document_ object with various _Element_ as input.

For example, if you have a User object in your system, you can easily convert it to a Document with this simple builder
pattern provided

```
new Document.Builder(User.getId())
    .addElement(new Element.Builder().setType(TEXT).setValue(User.getName()).createElement())
    .addElement(new Element.Builder().setType(ADDRESS).setValue(User.getAddress()).createElement())
    .addElement(new Element.Builder().setType(PHONE).setValue(User.getPhone()).createElement())
    .addElement(new Element.Builder().setType(EMAIL).setValue(User.getEmail()).createElement())
    .createDocument();
```

The Element Types like "TEXT", "ADDRESS", "PHONE", "EMAIL" are just simple ways to define custom stages (or functions)
to be applied at different stages of match.
 
Below is the list of _Element Types_ available in the library with default _PreProcessing Function_, _Tokenizer Function_ and _Scoring Function_.

|  Element Type | PreProcessing Function | Tokenizer Function | Scoring Function |
|:-------------:|------------------------|-----------------------|------------------|
|   ___NAME___  |   namePreprocessing()  |    wordTokenizer()    |     soundex()    |
|   ___TEXT___  |  removeSpecialChars()  |    wordTokenizer()    |     soundex()    |
| ___ADDRESS___ | addressPreprocessing() |    wordTokenizer()    |     soundex()    |
|  ___EMAIL___  |     removeDomain()     |    nGramTokenizer()   |    equality()    |
|  ___PHONE___  |     numericValue()     |    valueTokenizer()   |   phoneNumber()  |

### Applying the Match
The entry point for running this program is through MatchService class.
Import Match service using spring in your class

```
@Autowired
MatchService matchService;
```

It support 3 ways to match the documents

* __Match a list of Documents__ : This is useful if you have an existing list of document, and want to find out which
of them might have potential duplicates. A typical de-dup use case

```
matchService.applyMatch(List<Document> documents)
```

* __Match a list of Document with Existing List__ : This is useful for matching a new list of document with an existing
list in your system. For example if your are performing a bulk import and want to find out if any of them match with
existing data

```
matchService.applyMatch(List<Document> documents, List<Document> matchWith)
```

* __Match a Document with Existing List__ : This is useful when a new document is being created and you want to ensure
that a similar document does not already exist in your system

```
matchService.applyMatch(Document document, List<Document> matchWith)
```

### Output
The response of the library is essentially a ```Match<Document>``` object. It has 3 attributes
* __Data__ : This is the source Document on which the match is applied
* __MatchedWith__ : This is the target Document that the data matched with
* __Result__ : This is the probability score between 0.0 - 1.0 indicating how similar the 2 documents are

The response is grouped by the _Data_ attribute, so from any of the MatchService methods the response is map

```Map<Document, List<Match<Document>>>```

## End User Configuration
This library allows configuration at various level. These are all the configurable elements allowed

### Configurable Functions
All the 4 Stages defined above are configurable java functions passed into the library. Although there is a rich set of
predefined functions, they can easily be overridden or applied in addition of the exiting functions.

For example if an _ElementType_ is defined as a _TEXT_ the ```removeSpecialChars``` which is a simple transform
function is applied. This is a ```Function<String, String>``` that converts a String to another String

If you want change this and say in "Name" field remove any suffix like "jr.", "sr.", etc

This can be over-ridden when creating an Element using the builder pattern
```
new Element.Builder().setType(TEXT).setValue(user.getName())
                            .setPreProcessingFunction(str -> str.replace("jr.", ""))
                            .createElement())
```

Using the function composition, you could also add more functionality to the existing function
```
new Element.Builder().setType(TEXT).setValue(user.getName())
                            .setPreProcessingFunction(removeSpecialChars().compose(str -> str.replace("jr.", ""))
                            .createElement())
```

This allows you to change the entire behavior of how matches are applied at all the 4 stages

### Document Configuration
* __Key__ : Required field indicating unique primary key of the document
* __Elements__ : Set of elements for each document
* __Threshold__ : A double value between 0.0 - 1.0 above which the document be considered as match.
* __ScoringFunction__ : The _ScoringFunction_ used to aggregate individual _Element_ scores to a _Document_ score

### Element Configuration
* __Value__ : String representation of the value to match
* __Type__ : These are predefined elements, which applies relevant functions for "PreProcessing", "Tokenization" and "SimilarityMatch"
* __Threshold__ : A double value between 0.0 - 1.0 above which the element be considered as match.
* __Weight__ : A value applied to an element to increase or decrease the document score.
    The default is 1.0, any value above that will increase the document score if that element is matched.
* __PreProcessingFunction__ : The _PreProcessingFunction_ function to be used
* __TokenizerFunction__ : The _TokenizerFunction_ to be used
* __SimilarityMatchFunction__ : The _SimilarityMatchFunction_ to be used
* __ScoringFunction__ : The _ScoringFunction_ used to aggregate individual _Token_ scores to an _Element_ score


## Demo
To run a simple match. Consider this example of names (in file src/test/resources/demo.csv)
```
Stephen Wilkson
John Pierre
Wilson, Stephen
Pierre john
Stephen Kilsman wilkson
```

Run this maven test for the above set of example
```
mvn -Dtest=com.intuit.fuzzymatcher.component.MatchServiceTest#itShouldApplyMatchForDemo test
```

You should see a response like this printed on your console
```
Data: {[{'Stephen Wilkson'}]} Matched With: {[{'Stephen Kilsman wilkson'}]} Score: 0.6666666666666666
Data: {[{'John Pierre'}]} Matched With: {[{'Pierre john'}]} Score: 1.0
Data: {[{'Pierre john'}]} Matched With: {[{'John Pierre'}]} Score: 1.0
Data: {[{'Stephen Kilsman wilkson'}]} Matched With: {[{'Stephen Wilkson'}]} Score: 0.6666666666666666
```

Change the input and see the results change .... Happy Matching!!!