# Contributing to fuzzy-matcher

Thanks for your interest in fuzzy-matcher.

## Getting Started

Readme.md file gives a good overview of the architecture. Make sure to review the readme.

## Building the Library
### Prerequisite
You need Java JDK 11 or higher. Before you begin, you should check your current Java installation by using the following command:
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

## Contributions

fuzzy-matcher welcomes contributions from everyone.

Contributions to fuzzy-matcher should be made in the form of GitHub pull requests. Each pull request will
be reviewed by a core contributor (someone with permission to land patches) and either landed in the
main tree or given feedback for changes that would be required.

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Commits should be as small as possible, while ensuring that each commit is
  correct independently (i.e., each commit should compile and pass tests).

- If your patch is not getting reviewed or you need a specific person to review
  it, you can @-reply a reviewer asking for a review in the pull request or a
  comment.

- Add tests relevant to the fixed bug or new feature.

## Conduct

All code in this repository is under the Apache Software Foundation License, 2.0.