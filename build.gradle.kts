@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
    `groovy-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    idea

    `java-library`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.palantir.git-version") version "3.1.0" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "org.wiremock.tools.gradle"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    website = "https://github.com/wiremock/gradle-wiremock-extension-plugins"
    vcsUrl = "https://github.com/wiremock/gradle-wiremock-extension-plugins"
    plugins {
        afterEvaluate {
            removeIf { it.id.equals("org.wiremock.tools.gradle.wiremock-extension-convention") }
            register("wiremock-extension-convention") {
                id = "org.wiremock.tools.gradle.wiremock-extension-convention"
                implementationClass = "OrgWiremockToolsGradleWiremockExtensionConventionPlugin"
                displayName = "Gradle convention plugin that bundles common packaging and release logic for WireMock extensions"
                description = "Gradle convention plugin for WireMock Extensions"
                tags = listOf("wiremock")
            }
        }
    }
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.palantir.gradle.gitversion:gradle-git-version:3.1.0")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")

    runtimeOnly(kotlin("gradle-plugin"))

    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13.2")
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

val wiremockVersion = "3.6.0"

val basePackagePath = "org/wiremock/tools/gradle/plugins"
val processResources by tasks.existing(ProcessResources::class)
val writeDefaultVersionsProperties by tasks.registering(WriteProperties::class) {
    destinationFile.set(processResources.get().destinationDir.resolve("$basePackagePath/default-versions.properties"))
    property("wiremock_version", wiremockVersion)
}
processResources {
    dependsOn(writeDefaultVersionsProperties)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = "wiremock-extension-convention"
            version = "${project.version}"

            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}
