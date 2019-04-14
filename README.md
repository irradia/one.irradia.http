one.irradia.http
===

[![Build Status](https://img.shields.io/travis/irradia/one.irradia.http.svg?style=flat-square)](https://travis-ci.org/irradia/one.irradia.http)
[![Maven Central](https://img.shields.io/maven-central/v/one.irradia.http/one.irradia.http.api.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22one.irradia.http%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/https/oss.sonatype.org/one.irradia.http/one.irradia.http.api.svg?style=flat-square)](https://oss.sonatype.org/content/repositories/snapshots/one.irradia.http/)
[![Codacy Badge](https://img.shields.io/codacy/grade/CODACY_GRADE.svg?style=flat-square)](https://www.codacy.com/app/github_79/one.irradia.http?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=irradia/one.irradia.http&amp;utm_campaign=Badge_Grade)
[![Codecov](https://img.shields.io/codecov/c/github/irradia/one.irradia.http.svg?style=flat-square)](https://codecov.io/gh/irradia/one.irradia.http)
[![Gitter](https://badges.gitter.im/irradia-org/community.svg)](https://gitter.im/irradia-org/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

![http](./src/site/resources/http.jpg?raw=true)

## Building

Install the Android SDK.

```
$ ./gradlew clean assembleDebug test
```

If the above fails, it's a bug. Report it!

## Using

Use the following Maven or Gradle dependencies, replacing `${LATEST_VERSION_HERE}` with
whatever is the latest version published to Maven Central:

```
<!-- API -->
<dependency>
  <groupId>one.irradia.http</groupId>
  <artifactId>one.irradia.http.api</artifactId>
  <version>${LATEST_VERSION_HERE}</version>
</dependency>

<!-- Default implementation -->
<dependency>
  <groupId>one.irradia.http</groupId>
  <artifactId>one.irradia.http.vanilla</artifactId>
  <version>${LATEST_VERSION_HERE}</version>
</dependency>
```

```
repositories {
  mavenCentral()
}

implementation "one.irradia.http:one.irradia.http.api:${LATEST_VERSION_HERE}"
implementation "one.irradia.http:one.irradia.http.vanilla:${LATEST_VERSION_HERE}"
```

Library code is encouraged to depend only upon the API package in order to give consumers
the freedom to use other implementations of the API if desired.

## Publishing Releases

Releases are published to Maven Central with the following invocation:

```
$ ./gradlew clean assembleDebug publish closeAndReleaseRepository
```

Consult the documentation for the [Gradle Signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
and the [Gradle Nexus staging plugin](https://github.com/Codearte/gradle-nexus-staging-plugin/) for
details on what needs to go into your `~/.gradle/gradle.properties` file to do the appropriate
PGP signing of artifacts and uploads to Maven Central.

## Semantic Versioning

All [irradia.one](https://www.irradia.one) packages obey [Semantic Versioning](https://www.semver.org)
once they reach version `1.0.0`.
