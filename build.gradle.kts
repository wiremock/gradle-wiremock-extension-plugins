import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.internal.hash.Hashing

plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    `idea`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    `signing`
    `java-library`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.palantir.git-version") version "3.0.0"
    id("wiremock-extension-convention2")
}

group = "org.wiremock.tools.gradle"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    gradlePluginPortal()
}

gradlePlugin {
    website = "https://github.com/wiremock/gradle-wiremock-extension-plugins"
    vcsUrl = "https://github.com/wiremock/gradle-wiremock-extension-plugins"
    plugins {
        register("wiremock-extension-convention") {
            id = "org.wiremock.tools.gradle.wiremock-extension-convention"
            implementationClass = "org.wiremock.tools.gradle.conventions.WireMockExtensionConventionPlugin"
            displayName = "Gradle convention plugin that bundles common packaging and release logic for WireMock extensions"
            description = "Gradle convention plugin for WireMock Extensions"
            tags = listOf("wiremock")
        }
    }
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    runtimeOnly(kotlin("gradle-plugin"))

    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
}

tasks {
    validatePlugins {
        enableStricterValidation.set(true)
        failOnWarning.set(true)
    }
    jar {
        from(sourceSets.main.map { it.allSource })
        manifest.attributes.apply {
            put("Implementation-Title", "WireMock Convention Plugin (${project.name})")
            put("Implementation-Version", archiveVersion.get())
        }
    }
}

// default versions ---------------------------------------------------

val wiremockVersion = "3.3.1"

val basePackagePath = "org/wiremock/tools/gradle/plugins"
val processResources by tasks.existing(ProcessResources::class)
val writeDefaultVersionsProperties by tasks.registering(WriteProperties::class) {
    outputFile = processResources.get().destinationDir.resolve("$basePackagePath/default-versions.properties")
    property("wiremock_version", wiremockVersion)
}
processResources {
    dependsOn(writeDefaultVersionsProperties)
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}
