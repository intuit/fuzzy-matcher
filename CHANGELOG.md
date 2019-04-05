# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

