# <img src="fuzzy-logo.svg" height="60" width="60"/> Fuzzy-Matcher
* [Introduction](#introduction)
    * [What's Fuzzy](#whats-fuzzy)
* [How does this work](#how-does-this-work)
    * [Breaking down your data](#breaking-down-your-data)
    * [Four Stages of Fuzzy Match](#four-stages-of-fuzzy-match)
* [End User Configuration](#end-user-configuration)
    * [Predefined Element Types](#predefined-element-types)
    * [Document Configuration](#document-configuration)
    * [Match Service](#match-service)
    * [Match Results](#match-results)
* [Quick Start](#quick-start)
    * [Maven Import](#maven-import)
    * [Input](#input)
    * [Applying the Match](#applying-the-match)
    * [Output](#output)
* [Performance](#performance)


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

This combination to tokenize the data and then to match them can extract similarity in a wide variety of data types

#### Exact word match
Consider these Elements defined in two different Documents
 * Wayne Grace Jr.
 * Grace Hilton Wayne

With a simple tokenization process each word here can be considered a token, and if another element has the same word 
they are scored on the number of matching tokens.  In this example the words `Wayne` and `Grace` match 2 words out of 
3 total in each elements. A scoring mechanism will match them with a result of 0.67

#### Soundex word match
Consider these Elements in two different Documents
 * Steven Wilson
 * Stephen Wilkson

Here we do not just look at each word, but encode it using Soundex which gives a unique code for the phonetic spelling of the name. 
So in this example words `Steven` & `Stephen` will encode to `S315` whereas the words `Wilson` & `Wilkson` encode to `W425`.

This allows both the elements to match exactly, and score at 1.0

#### NGram token match
In cases where breaking down the Elements in words is not feasible, we split it using NGrams. Take for examples emails
 * parker.james@gmail.com
 * james_parker@yahoo.com
 
Here if we ignore the domain name and take 3 character sequence (tri-gram) of the data, tokens will look like this

* parker.james -> [par, ark, rke, ker, er., r.j, .ja, jam, ame, mes]
* james_parker -> [jam, ame, mes, es_, s_p, _pa, par, ark, rke, ker] 

Comparing these NGrams we have 7 out of the total 10 tokens match exactly which gives a score of 0.7

#### Nearest Neighbors match
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
the measure of matching similar data. `PreProcessing` your data is a simple yet powerful mechanism that can help in starting 
with clean data before running a match. These 4 stages which are highly customizable can be used to tune and match a wide variety of data types   


* __Pre-Processing__ : This accepts a java `Function`. Which allows you to externally develop the pre-processing functionality and pass it to the library. 
Or use some of the existing ones. These are a few examples that are already available
    * _Trim_: Removes leading and trailing spaces (applied by default)
    * _Lower Case_: Converts all characters to lowercase (applied by default)
    * _Remove Special Chars_ : Removes all characters except alpha and numeric characters and spaces. (default for _TEXT_ type)
    * _Numeric_: Strips all non-numeric characters. Useful for numeric values like phone or ssn (default for _NUMBER_ type)
    * _Email_: Strips away domain from an email. This prevents common domains like gmail.com, yahoo.com to be considered in match (default for _EMAIL_ type)

* __Tokenization__ : This again accepts a `Function` so can be externally defined and fed to the library. 
Some commonly used are already available.
    * _Word_ : Breaks down an element into words (anything delimited by space " ").
    * _N-Gram_ : Breaks down an element into 3 letter grams.
    * _Word-Soundex_ : Breaks down in words (space delimited) and gets Soundex encode using the Apache Soundex library
    * _Value_ : Nothing to break down here, just uses the element value as token. Useful for Nearest Neighbor matches

* __Match Type__ : Allows 2 types of matches, which can be applied to each `Element`
    * _Equality_: Uses exact matches with token values. 
    * _Nearest Neighbor_: Finds tokens that are contained in the neighborhood range, that can be specified as a 
    probability (0.0 - 1.0) for each element. It defaults to 0.9 
    
* __Scoring__ : These are defined for `Element` and `Document` matches
    * _Element scoring_: Uses a simple average, where for each element the matching token is divided by the total tokens. 
    A configurable `threshold` can be set for each element beyond which elements are considered to match (default set at 0.3)
    * _Document scoring_: A similar approach where number of matching elements are compared with total element. 
    In addition, each element can be give a `weight`. This is useful when some elements in a document are considered more significant than others. 
    A `threshold` can also be specified at a document level (defaults to 0.5) beyond which documents are considered to match

## End User Configuration
All the configurable options defined above can be applied at various points in the library.

### Predefined Element Types
 
Below is the list of predefined _Element Types_ available with sensible defaults. These can be overridden by `setters` while creating an `Element`. 

|  Element Type | PreProcessing Function | Tokenizer Function           | Match Type       |
|---------------|------------------------|------------------------------|--------------------|
| ___NAME___    | namePreprocessing()    | wordSoundexEncodeTokenizer() |     EQUALITY       |
| ___TEXT___    | removeSpecialChars()   | wordTokenizer()              |     EQUALITY       |
| ___ADDRESS___ | addressPreprocessing() | wordSoundexEncodeTokenizer() |     EQUALITY       |
| ___EMAIL___   | removeDomain()         | triGramTokenizer()           |     EQUALITY       |
| ___PHONE___   | numericValue()         | decaGramTokenizer()          |     EQUALITY       |
| ___NUMBER___  | numberPreprocessing()  | valueTokenizer()             | NEAREST_NEIGHBORS |
| ___DATE___    | none()                 | valueTokenizer()             | NEAREST_NEIGHBORS |
| ___AGE___  | numberPreprocessing()  | valueTokenizer()             | NEAREST_NEIGHBORS |

_Note: Since each element is unique in the way it should match, if you need to match a different element type than
 what is supported, please open a new [GitHub Issue](https://github.com/intuit/fuzzy-matcher/issues) and the community 
 will provide support and enhancement to this library_
    
### Document Configuration
* __Key__: Required field indicating unique primary key of the document
* __Elements__: Set of elements for each document
* __Threshold__: A double value between 0.0 - 1.0, above which the document is considered as match.

### Element Configuration
* __Value__ : String representation of the value to match
* __Type__ : These are predefined elements, which apply relevant functions for "PreProcessing", "Tokenization" and "MatchType"
* __Variance__: (Optional) To differentiate same element types in a document. eg. a document containing 2 NAME element one for "user" and one for "spouse"
* __Threshold__: A double value between 0.0 - 1.0, above which the element is considered as match.
* __Weight__: A value applied to an element to increase or decrease the document score.
    The default is 1.0, any value above that will increase the document score if that element is matched.
* __PreProcessingFunction__: Override The _PreProcessingFunction_ function defined by Type
* __TokenizerFunction__: Override The _TokenizerFunction_ function defined by Type
* __MatchType__: Override the MatchType defined by Type
* __NeighborhoodRange__: Relevant only for `NEAREST_NEIGHBORS` MatchType. Defines how close should the `Value` be, to be considered a match. 
Accepted values between 0.0 - 1.0 (defaults to 0.9) 

### Match Service
It supports 3 ways to match the documents

* __Match a list of Documents__: This is useful if you have an existing list of documents, and want to find out which
of them might have potential duplicates. A typical de-dup use case

```
matchService.applyMatchByDocId(List<Document> documents)
```

* __Match a list of Documents with an Existing List__: This is useful for matching a new list of documents with an existing
list in your system. For example, if you're performing a bulk import and want to find out if any of them match with
existing data

```
matchService.applyMatchByDocId(List<Document> documents, List<Document> matchWith)
```

* __Match a Document with Existing List__: This is useful when a new document is being created and you want to ensure
that a similar document does not already exist in your system

```
matchService.applyMatchByDocId(Document document, List<Document> matchWith)
```

### Match Results
The response of the library is essentially a ```Match<Document>``` object. It has 3 attributes
* __Data__: This is the source Document on which the match is applied
* __MatchedWith__: This is the target Document that the data matched with
* __Result__: This is the probability score between 0.0 - 1.0 indicating how similar the 2 documents are

The response is grouped by the _Data_.`key`, so from any of the MatchService methods the response is map

```Map<String, List<Match<Document>>>```


## Quick Start

### Maven Import
The library is published to maven central
```
<dependency>
    <groupId>com.intuit.fuzzymatcher</groupId>
    <artifactId>fuzzy-matcher</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Input
This library takes a collection of _Document_ objects with various _Elements_ as input.

For example, if you have a multiple contacts as a simple String Arrays

```
String[][] input = {
        {"1", "Steven Wilson", "45th Avenue 5th st."},
        {"2", "John Doe", "546 freeman ave"},
        {"3", "Stephen Wilkson", "45th Ave 5th Street"}
};
```
Convert them into List of Document

```
List<Document> documentList = Arrays.asList(input).stream().map(contact -> {
    return new Document.Builder(contact[0])
            .addElement(new Element.Builder<String>().setValue(contact[1]).setType(NAME).createElement())
            .addElement(new Element.Builder<String>().setValue(contact[2]).setType(ADDRESS).createElement())
            .createDocument();
}).collect(Collectors.toList());
```


### Applying the Match
The entry point for running this program is through MatchService class.
Create a new instance of Match service, and use applyMatch methods to find matches

```
MatchService matchService = new MatchService();
Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(documentList);
```

### Output
This prints the result to console. This should show a match between the 1st and 3rd document, but not the 2nd.
```
result.entrySet().forEach(entry -> {
    entry.getValue().forEach(match -> {
        System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
    });
});
```             

## Performance
For most real life data-sets, the size of the data I am sure is not as simple as shown in the examples.   
Since this library can be used to match elements against a large set of records, knowing how it performs is essential.

The performance characteristics varies primarily on MatchType being used

* EQUALITY - For equality match, which is the default for most Element Types, the performance is linear O(N). 
Where N is the number of `Element` in all the document.

* NEAREST_NEIGHBOR - The default for Numeric and Date Element Types the performance is O(N logN). 
This also depends on the `NeighborhoodRange` setting , the higher the value the better it will perform. 
It is advisable to not use 1.0 as a `NeighborhoodRange` and instead over-ride the `MatchType` to be `EQUALITY`, 
that way it guarantees a linear performance.

The following chart shows the performance characteristics of this library as the number of elements increase. As you can see, the 
library maintains a near-linear performance and can match thousands of elements within seconds on a multi-core processor.

![Perf](perf.png?raw=true "Performance")
