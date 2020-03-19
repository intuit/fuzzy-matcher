# <img src="fuzzy-logo.svg" height="60" width="60"/> Fuzzy-Matcher
* [Introduction](#introduction)
* [How does this work](#how-does-this-work)
    * [Four Stages of Fuzzy Match](#four-stages-of-fuzzy-match)
    * [Performance](#Performance)
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
A java-based library to match and group "similar" elements in a collection of documents.

Imagine working in a system with a collection of contacts and wanting to match and categorize contacts with similar 
names, addresses or other attributes. The Fuzzy Match matching algorithm can help you do this. 
The Fuzzy Match algorithm can even help you find duplicate contacts, or prevent your system from adding duplicates.

This library can act on any domain object, like contact, and find similarity for various use cases. 
It dives deep into each character and finds out the probability that 2 or more objects are similar.


### What's Fuzzy
The contacts `"Steven Wilson" living at "45th Avenue 5th st."` and `"Stephen Wilkson" living at "45th Ave 5th Street"` 
might look like belonging to the same person. It's easy for humans to ignore the small variance in spelling in names,
or ignore abbreviation used in address. But for a computer program they are not the same. The string `Steven` does not 
equals `Stephen` and neither does `Street` equals `st.`
If our trusted computers can start looking at each character and the sequence in which they appear, it might look similar.
Fuzzy matching algorithms is all about providing this level of magnification to our myopic machines.

## How does this work
### Breaking down your data
This algorithm accepts data in a list of entities called `Document` (like a contact entity in your system), which can contain 1 
or more `Element` (like names, address, emails, etc). Internally each element is further broken down into 1 or more `Token` 
which are then matched using configurable `MatchType`

This combination to tokenize and matching can figure out similarity in a wide variety of data types
#### Exact word match
Consider these Element's in different Document's
 * Wayne Grace Jr.
 * Grace Hilton Wayne

Each word is considered a token, and if another element has the exact match they are scored on the number of matching tokens. 
In this example the words `Wayne` and `Grace` match 2 words out of 3 total in each elements.
A scoring mechanism will match them with a result of 0.67

#### Soundex word match
Consider these Element's in different Document's
 * Steven Wilson
 * Stephen Wilkson

Here we do not just look at each word, but encode it using Soundex which gives a unique code for the phonetic spelling of the name. 
So in this example words `Steven` & `Stephen` will encode to `S315` whereas the words `Wilson` & `Wilkson` encode to `W425`.

This allows both the elements to match exactly, and score at 1.0

#### NGram token match
In cases where breaking down the Element's in words is not feasible, we split it using NGrams. Take for examples email's
 * parker.james@gmail.com
 * james_parker@yahoo.com
 
Here if we ignore the domain name and take 3 character sequence (tri-gram) of the data, token's will look like this

* parker.james -> [par, ark, rke, ker, er., r.j, .ja, jam, ame, mes]
* james_parker -> [jam, ame, mes, es_, s_p, _pa, par, ark, rke, ker] 

Comparing these NGrams we have 7 out of the total 10 tokens match exactly which gives a score of 0.7

#### Nearest Neighbours match
In certain cases breaking down elements into tokens and comparing tokens is not an option. 
For example numeric values, like dollar amounts in a list of transactions

* 100.54
* 200.00
* 100.00

Here the first and third could belong to the same transaction, where the third is only missing some precession. 
The match is done not on tokens being equal but on the closeness (the neighborhood range) in which the values appear.
This closeness is again configurable where a 99% closeness, will match them with a score of 1.0

A similar example can be thought of with Dates, where dates that are near to each other might point to the same event.             

### Four Stages of Fuzzy Match
![Fuzzy Match](fuzzy-match.png?raw=true "Fuzzy Match")

We spoke in detail on `Token` and `MatchType` which is the core of fuzzy matching, and touched upon `Scoring` which gives 
the measure of matching similar data. `PreProcessing` your data is a simple yet powerful mechanism that can help in staring 
with a clean data before running a match. These 4 stages which are highly customizable can be used to tune and match a wide variety of data types   


* __Pre-Processing__ : This accepts a java `Function`. Which allows you to externally code it and pass it to the library. 
Some examples of pre-processing that are available in the library are.
    * _Trim_: Removes leading and trailing spaces (applied by default)
    * _Lower Case_: Converts all characters to lowercase (applied by default)
    * _Remove Special Chars_ : Removes all characters except alpha and numeric characters and spaces. (default for _TEXT_ type)
    * _Numeric_: Strips all non-numeric characters. Useful for numeric values like phone or ssn (default for _PHONE_ type)
    * _Email_: Strips away domain from an email. This prevents common domains like gmail.com, yahoo.com to be considered in match (default for _EMAIL_ type)

* __Tokenization__ : This again accepts a `Function` so can be externally defined and fed to the library. 
But a host of commonly used are already defined. Examples
    * _Word_ : Breaks down an element into words (anything delimited by space " ").
    * _N-Gram_ : Breaks down an element into 3 letter grams.
    * _Word-Soundex_ : Breaks down in words (space delimited) and gets Soundex encode using the Apache Soundex library
    * _Value_ : Nothing to break down here, just uses the element value as token. Useful for Nearest Neighbour matches

* __Match Type__ : Allows 2 types of matches
    * _Equality_: Uses exact matches with token values. 
    * _Nearest Neighbor_: Finds tokens that are contained in the neighborhood range, that can be specified as a 
    probability (0.0 - 1.0) for each element. It defaults to 0.9 
    
* __Scoring__ : Expects a ```BiFunction<Match, List<Score>, Double>```, this defines functions on how to accumulate scores 
    from Tokens into Elements and from Elements into Documents.
    * _Simple Average_: Adds up total scores of each child matches / total children. This is the default scoring for Elements
    * _Weighted Average_: This is useful for Document Scoring, where users can input weights on elements.
        Example: a phone number or email could be considered an important element to identify match between 2 User objects, and we can add weights to such elements.
    * _Exponential Average_: Again useful for Document Scoring, where if more than 1 elements match, we can increase the scoring exponentially
    * _Exponential Weighted Average_: Uses both exponents and weights for scoring. This is the default for Document Scoring

### Performance
Since this library can be used to match elements against a large set of records, knowing how it performs is essential.


This library makes use of Java 8 Stream to run all operations in parallel and makes optimum use of a multi-core cpu.
Beyond finding duplicates in a given set of records, this library avoids matching each element with every other 
element, and reduces the complexity which otherwise would be O(N^2)

#### Reducing Complexity to O(N Log N)
To reduce the complexity, the similarity match algorithms chosen are assumed to have an equivalence property. Where, if 
a name like "Stephen" matches with "Steven" with a score of 1.0, the reverse match is assumed, and the library does
not explicitly run those matches

#### Search Groups
The library further reduces the complexity by not performing matches against the elements which have a very low 
probability to match by creating "Search Groups". Take an example of list of names to match ```["steve","parker","stephen"]```
These names are broken down into tri-grams like this
```
steve -> [ste,tev,eve]
parker -> [par,ark,rke,ker]
stephen -> [ste,tep,eph,phe,hen] 
```    
Here, only the 1st and 3rd names have tri-grams "ste" in common (and a search group is created for them.)  
The match algorithm assumes a very low probability that "parker" will match with the other 2, and hence no match is attempted with it. 

The following chart shows the performance characteristics of this library as the number of elements increase. As you can see, the 
library maintains a near-linear performance and can match thousands of elements within seconds on a multi-core processor.

![Perf](perf.png?raw=true "Performance")

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
After cloning the project locally, run this command to compile, test and install the project
```
mvn clean install
```

## Using the Library

### Maven Import
The library is published to maven central
```
<dependency>
    <groupId>com.intuit.fuzzymatcher</groupId>
    <artifactId>fuzzy-matcher</artifactId>
    <version>0.4.4</version>
</dependency>
```

### Input
This library takes a collection of _Document_ objects with various _Elements_ as input.

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

|  Element Type | PreProcessing Function | Tokenizer Function    | Scoring Function         |
|:-------------:|------------------------|-----------------------|--------------------------|
|   ___NAME___  |   namePreprocessing()  |    wordTokenizer()    |     soundex()            |
|   ___TEXT___  |  removeSpecialChars()  |    wordTokenizer()    |     soundex()            |
| ___ADDRESS___ | addressPreprocessing() |    wordTokenizer()    |     soundex()            |
|  ___EMAIL___  |     removeDomain()     |    nGramTokenizer()   |    equality()            |
|  ___PHONE___  |     numericValue()     |    valueTokenizer()   |   phoneNumber()          |
|  ___NUMBER___ | numberPreprocessing()  |    valueTokenizer()   |numberDifferenceRate()    |
|  ___DATE___   | none()                 |    valueTokenizer()   |dateDifferenceWithinYear()|

_Note: Since each element is unique in the way it should match, if you need to match a different element type than
 what is supported, please open a new [GitHub Issue](https://github.com/intuit/fuzzy-matcher/issues) and the community 
 will provide support and enhancement to this library_

### Applying the Match
The entry point for running this program is through MatchService class.
Create a new instance of Match service.

```
MatchService matchService = new MatchService();
```

It supports 3 ways to match the documents

* __Match a list of Documents__: This is useful if you have an existing list of documents, and want to find out which
of them might have potential duplicates. A typical de-dup use case

```
matchService.applyMatch(List<Document> documents)
```

* __Match a list of Documents with an Existing List__: This is useful for matching a new list of documents with an existing
list in your system. For example, if you're performing a bulk import and want to find out if any of them match with
existing data

```
matchService.applyMatch(List<Document> documents, List<Document> matchWith)
```

* __Match a Document with Existing List__: This is useful when a new document is being created and you want to ensure
that a similar document does not already exist in your system

```
matchService.applyMatch(Document document, List<Document> matchWith)
```

### Output
The response of the library is essentially a ```Match<Document>``` object. It has 3 attributes
* __Data__: This is the source Document on which the match is applied
* __MatchedWith__: This is the target Document that the data matched with
* __Result__: This is the probability score between 0.0 - 1.0 indicating how similar the 2 documents are

The response is grouped by the _Data_ attribute, so from any of the MatchService methods the response is map

```Map<Document, List<Match<Document>>>```

## End User Configuration
This library allows configuration at various levels. These are all the configurable elements allowed

### Configurable Functions
All the 4 Stages defined above are configurable java functions passed into the library. Although there is a rich set of
predefined functions, they can easily be overridden or applied in addition to the existing functions.

For example, if an _ElementType_ is defined as a _TEXT_ the ```removeSpecialChars``` which is a simple transform
function is applied. This is a ```Function<String, String>``` that converts a String to another String

If you want to change this and say in "Name" field remove any suffix like "jr.", "sr.", etc

This can be overridden when creating an Element using the builder pattern
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

This allows you to change the entire behavior of how matches are applied at all 4 stages

### Document Configuration
* __Key__: Required field indicating unique primary key of the document
* __Elements__: Set of elements for each document
* __Threshold__: A double value between 0.0 - 1.0, above which the document is considered as match.
* __ScoringFunction__: The _ScoringFunction_ used to aggregate individual _Element_ scores to a _Document_ score

### Element Configuration
* __Value__ : String representation of the value to match
* __Type__ : These are predefined elements, which apply relevant functions for "PreProcessing", "Tokenization" and "SimilarityMatch"
* __Variance__: (Optional) To differentiate same element types in a document. eg. a document containing 2 NAME element one for "user" and one for "spouse"
* __Threshold__: A double value between 0.0 - 1.0, above which the element is considered as match.
* __Weight__: A value applied to an element to increase or decrease the document score.
    The default is 1.0, any value above that will increase the document score if that element is matched.
* __PreProcessingFunction__: The _PreProcessingFunction_ function to be used
* __TokenizerFunction__: The _TokenizerFunction_ to be used
* __SimilarityMatchFunction__: The _SimilarityMatchFunction_ to be used
* __ScoringFunction__: The _ScoringFunction_ used to aggregate individual _Token_ scores to an _Element_ score


## Demo
To run a simple match, consider this example of names (in file src/test/resources/demo.csv)
```
Stephen Wilkson
John Pierre
Wilson, Stephen
Pierre john
Stephen Kilsman wilkson
```

Run this maven test for the above example set
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
