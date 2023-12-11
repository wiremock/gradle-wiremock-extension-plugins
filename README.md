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

### Basic use

Set the following `gradle.properties`:

```properties
baseArtifact = my-test-wiremock-extension
version = 1.0.0-SNAPSHOT
description = My Test WireMock Extension
githubRepo = wiremock-my-test-extension
developer.id=todo
developer.name=TODO WireMock Developer
developer.email=noreply@wiremock.org
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
    id("org.wiremock.tools.gradle.wiremock-extension-convention")
}
```

### Shading

When you need to include additional dependencies,
they should be shaded in the `shadedJar` task:

```kotlin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
                
dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

tasks {
    named<ShadowJar>("shadowJar") {
        relocate("com.github.ben-manes.caffeine", "wiremock.com.github.ben-manes.caffeine")
        relocate("com.github.jknack", "wiremock.com.github.jknack")
    }
}
```

## Usage Examples

- [WireMock Faker Extension](https://github.com/wiremock/wiremock-faker-extension)
- [WireMock State Extension](https://github.com/wiremock/wiremock-state-extension)
