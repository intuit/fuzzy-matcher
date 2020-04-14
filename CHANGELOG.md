# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.3 - 2020-04-14
### Added
- MatchService.applyMatchByGroups new service method to return groups of Matches. 
Eg. A matches with B and B with C, all of them appear in the same group (set of matches) 

### Changed
- Token class now supports Generics for value instead of Object
- TokenizerFunction and PreProcessing Functions are no longer functional interface, but instead a Java Class with static methods that return standard java functions.
- ElementType does not take parameters in constructors and is now a simple enumeration. This is to allow easier JNBridge proxies for .net support 

### Fixed
- NEAREST_NEIGHBOR match issue with negative numbers and dates (before epoch) https://github.com/intuit/fuzzy-matcher/issues/28
- NEAREST_NEIGHBOR match not returning multiple matches if the matching value is exactly same.

## 1.0.0 - 2020-03-25
### Added
- In Element ability to set MatchType (this replaces similarityMatchFunction)
- In Element ability to  set NeighborhoodRange for NEAREST_NEIGHBOR MatchType
- New classes added - TokenRepo (replaces the TokenMatch) and MatchType (replaces SimilariyyMatchFunction) 

### Removed
- Document and Element classes does not allow to externally define a ScoringFunction. This is now set to defaults as 
SimpleAverage (in Element) and ExponentialWeightedAverage (in Document)
- Element does not allow to externally define similarityMatchFunction. This is replaced by MatchType
- Element does not allow to externally define matchOptimizerFunction. All these changes allow a guaranteed performance of the library
- These classes are removed - TokenMatch, NGram, MatchOptimizerFunction, SimilarityMatchFunction

### Changed
- Significant performance improvements along with reduced memory utilization
- Soundex match is no longer a Matching function, it is replaced as a tokenization function instead, where encoded soundex token are now used.
- Element is a generic now. Which replaces the `value` as generic instead of object.
- ElementType of TEXT is matched by word equality instead of Soundex matching function by default
- ElementType of NUMBER and DATE are matched using NEAREST_NEIGHBOR MatchType. This gives similar results, but are 
controlled by NeighborhoodRange attribute defined in Element instead of Threshold   

## 0.4.4 - 2019-12-23
### Fixed
- Ability to configure scoring function in Element https://github.com/intuit/fuzzy-matcher/issues/19
### Changed
- Improved memory utilization by processing all elements by it's type or classification. 
This allows jvm to run gc to free heap space after each element type is processed 

## 0.4.2 - 2019-08-09
### Added
- Element.Builder.setValue overridden to accept Object Type
### Removed
- Match.childScores is dropped to reduce memory footprint.
### Changed
- ScoringFunction changed to BiFunction from Function. It takes a List<Score> of childScores as the second input. 
This allows childScores to be stored in function stack when score is evaluated instead of the heap, and reduces the overall memory footprint of the library  

## 0.4.1 - 2019-07-03
### Fixed
- Reduced memory footprint by removing tokens not meeting match threshold  

## 0.4.0 - 2019-04-19
### Added
- Element Variance. This allows multiple ElementTypes to be added in same document. Addresses [issue](https://github.com/intuit/fuzzy-matcher/issues/1)
- New NUMBER ElementType, this allows fuzzy match with numbers and score numbers according to how close they are to each other in value
- New DATE ElementType, similar to numbers dates that are closer in values are scored higher
- MatchOptimizerFunction. This can be set at element level and allows reducing complexity of the match. With this tokens
 with low probability to match are eliminated and helps improve performance. This can configured now for each element type

### Changed
- Overridden ElementBuilder.setValue and now accepts Double and Date java type along with String
- Performance improvement, changed element default scoring and cached Token list for each element
- Within Element, Token and NGram classes "value" attribute is now Object type, instead of String. 
This allows the library to accept multiple java data type as input in the future 

## 0.3.0 - 2019-04-05
### Changed
- Performance improvements. Reduced complexity to O(N LogN)

## 0.2.1 - 2019-03-25
### Fixed
- Class path dependency using jar for name and address dictionary.

## 0.2.0 - 2019-03-22
### Removed
- Spring integration. With this the library is more light weight, and allows this to be used in non spring projects

## 0.1.0 - 2019-01-07
### Changed
- Ability to add unbalanced Element Type. The user no longer needs to add empty elements
- Improved scoring results where emtpy elements do not participate in overall score 
### Fixed
- fixed scoring to account for unbalanced elements in document

## 0.0.3 - 2018-12-21
### Changed
- Package structure changed to com.intuit.fuzzymatcher to match group id convention  

## 0.0.2 - 2018-12-21
### Added
- Integration with maven central
### Changed
- Group id changed com.intuit.fuzzymatcher from com.intuit.fm

## 0.0.1 - 2018-11-13
### Added
- Initial Version

