# Gradle Convention plugin for WireMock Extensions

This convention plugin helps to Build WireMock extensions
so that they can be hosted on Maven Central and
released automatically from GitHub actions.
See [the Maven Central Publishing documentation](https://github.com/wiremock/community/blob/main/infra/maven-central.md) to learn more.

Features:

- Code signing and POM generation for Maven Central publishing
- Proper shading of artifacts (work in progress)

## Requirements

- Gradle 8.x
- Java 11 or 17

## Usage

Set the following `gradle.properties`:

```properties
baseArtifact = my-test-wiremock-extension
version = 1.0.0-SNAPSHOT
description = My Test WireMock Extension
githubRepo = wiremock-my-test-extension 
```

Use the plugin in your `build.gradle.kts` file:

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
}
        
plugins {
    kotlin("jvm") version "$embeddedKotlinVersion"
    id("wiremock-extension-convention")
}
```
