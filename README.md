[version]: https://api.bintray.com/packages/kantenkugel/maven/ConsoleUtils/images/download.svg
[maven]: https://bintray.com/kantenkugel/maven/ConsoleUtils/_latestVersion
[buildStatus]: https://travis-ci.org/kantenkugel/ConsoleUtils.svg?branch=master
[buildLink]: https://travis-ci.org/kantenkugel/ConsoleUtils

ConsoleUtils - Using raw console input in java
==============================================

# Description
This Project uses raw console input (via JNA) to provide some utility methods.

Currently available features:
- Read raw input one char at a time (RawConsoleInput by Christian d'Heureuse [link](http://www.source-code.biz/snippets/java/RawConsoleInput))
- Read console input with placeholder chars or no output at all
- Read console input with given preexisting buffer
- Simple Auto-complete functionality (Experimental)

# Build-Status
We are using Travis-CI to validate our Builds.

Current Status: [![buildStatus][]][buildLink]

# License
This Project is licensed under the [Eclipse Public License v1.0](LICENSE.md)

# Getting the Project
You can get this Project from JCenter: [![version][]][maven] 

# Dependencies
ConsoleUtils uses Gradle for dependency management & distribution.

## Current Dependencies:
Runtime:
  - JNA *v4.5.0*
  
Testing:
  - JUnit *v4.12*
  - Mockito *v2.8 (2.9 not supported by PowerMock)*
  - PowerMock for JUnit and Mockito2 *v1.7.1*

## Contributing
Feel free to contribute to this Project by creating a PR.

Some guidelines for contributing:
- Try to match the already existing code style of the project (opening `{` on same line, ...).
- Please try to provide JUnit tests for any additions made (You can look at the pre-existing Tests for reference).
- Check if your changes broke already existing functionality by running `./gradlew check` (runs JUnit tests).
When Travis-CI fails to build due to some tests failing, the PR will not be merged!
- Provide meaningful and descriptive commit messages and a short description about your changes/additions in the PR.
- Add your name to the `@author` JDoc tag of the classes you modified/created
and optionally what exactly you did in JDoc description of class (to track contributions).