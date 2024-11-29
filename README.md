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

## Wiremock dependency

This extension has a bundled wiremock dependency. Depending on the wiremock features you're using
in your plugin, this plugin needs to be updated accordingly:

| Gradle Convention plugin version | Wiremock version |
|----------------------------------|------------------|
| 0.4.0+                           | 3.10.0           |
| 0.3.0+                           | 3.6.0            |
| 0.1.0+                           | 3.3.1            |


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

It can be tweaked a bit more, with some optional properties:

```properties
useShadowJar = false # true by default, false will avoid creating the standalone JAR
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

Or in Gradle:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id 'org.wiremock.tools.gradle.wiremock-extension-convention' version '0.1.2'
}
```

### Shading of dependencies

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

### Release Automation

To automate releases with this extension, add the following GitHub action in `.github/workflows/`:

```yaml
name: Release
on:
  release:
    types: [published]

jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'adopt'
      - name: Validate Gradle wrapper
        uses: gradle/wrapper-validation-action@v1

      - name: Determine new version
        id: new_version
        run: |
          NEW_VERSION=$(echo "${GITHUB_REF}" | cut -d "/" -f3)
          echo "new_version=${NEW_VERSION}" >> $GITHUB_OUTPUT

      - name: Publish package
        id: publish_package
        uses: gradle/gradle-build-action@v2.9.0
        with:
          arguments: -Pversion=${{ steps.new_version.outputs.new_version }} publish closeAndReleaseStagingRepository
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_TOKEN: ${{ secrets.OSSRH_TOKEN }}
          OSSRH_GPG_SECRET_KEY: ${{ secrets.OSSRH_GPG_SECRET_KEY }}
          OSSRH_GPG_SECRET_KEY_PASSWORD: ${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }}
```

Once the request for [Maven Central Publishing authorization](https://github.com/wiremock/community/blob/main/infra/maven-central.md) is completed,
you can trigger the release Pipeline by just publishing a GitHub Release.
Consider also configuring Release Drafter to automate chaangelogs.

## Usage Examples

- [WireMock Faker Extension](https://github.com/wiremock/wiremock-faker-extension)
- [WireMock State Extension](https://github.com/wiremock/wiremock-state-extension)

## Learn More

- "Comment nous utilisons Kotlin et Gradle pour faire évoluer la communauté WireMock" by Oleg Nenashev at Devoxx Fance 2024
  ([slides](https://speakerdeck.com/onenashev/devoxxfr-comment-nous-utilisons-kotlin-et-gradle-pour-faire-evoluer-la-communaute-wiremock),
   [video](https://www.youtube.com/watch?v=pToRVRTI-Zs))
