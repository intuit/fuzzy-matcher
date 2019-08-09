# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

