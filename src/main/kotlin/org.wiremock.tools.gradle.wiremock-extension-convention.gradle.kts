import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import gradle.kotlin.dsl.accessors._93599ad4e0f29edbb35935946eff8429.base
import gradle.kotlin.dsl.accessors._93599ad4e0f29edbb35935946eff8429.publishing
import gradle.kotlin.dsl.accessors._93599ad4e0f29edbb35935946eff8429.runtimeClasspath
import gradle.kotlin.dsl.accessors._93599ad4e0f29edbb35935946eff8429.signing
import org.gradle.plugins.signing.*

plugins {
    java
    `java-platform`
    id("java-library")
    idea
    id("signing")
    id("maven-publish")
    id("com.github.johnrengelman.shadow")
    id("io.github.gradle-nexus.publish-plugin")
}

group = "org.wiremock.extensions"

val baseArtifact = project.property("baseArtifact").toString()
val githubRepo = project.property("githubRepo").toString()
val versions = mapOf(
        "wiremock"    to "3.3.1",
        "junit"       to "5.10.1",
        "assertj"     to "3.24.2",
        "restAssured" to "5.3.2",
        "awaitility"  to "4.2.0",
        "testcontainers" to "1.19.3",
        "wiremockTestcontainers" to "1.0-alpha-13")
)

configurations {
    standaloneOnly
}


//tasks.jar {
//    this.baseArtifact = baseArtifact
//}


allprojects {
    java {

      //  withSourcesJar()
      //  withJavadocJar()
    }

}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName = "$baseArtifact-standalone"
        archiveClassifier = ""
        // configurations = arrayListOf(
        //     project.configurations.runtimeClasspath,
        //     project.configurations.getByName("standaloneOnly").toString()
        // )

        relocate("org.mortbay", "wiremock.org.mortbay")
        relocate("org.eclipse", "wiremock.org.eclipse")
        relocate("org.codehaus", "wiremock.org.codehaus")
        relocate("com.google", "wiremock.com.google")
        relocate("com.google.thirdparty", "wiremock.com.google.thirdparty")
        relocate("com.fasterxml.jackson", "wiremock.com.fasterxml.jackson")
        relocate("org.apache", "wiremock.org.apache")
        relocate("org.xmlunit", "wiremock.org.xmlunit")
        relocate("org.hamcrest", "wiremock.org.hamcrest")
        relocate("org.skyscreamer", "wiremock.org.skyscreamer")
        relocate("org.json", "wiremock.org.json")
        relocate("net.minidev", "wiremock.net.minidev")
        relocate("com.jayway", "wiremock.com.jayway")
        relocate("org.objectweb", "wiremock.org.objectweb")
        relocate("org.custommonkey", "wiremock.org.custommonkey")
        relocate("net.javacrumbs", "wiremock.net.javacrumbs")
        relocate("net.sf", "wiremock.net.sf")
        relocate("com.github.jknack", "wiremock.com.github.jknack")
        relocate("org.antlr", "wiremock.org.antlr")
        relocate("jakarta.servlet", "wiremock.jakarta.servlet")
        relocate("org.checkerframework", "wiremock.org.checkerframework")
        relocate("org.hamcrest", "wiremock.org.hamcrest")
        relocate("org.slf4j", "wiremock.org.slf4j")
        relocate("joptsimple", "wiremock.joptsimple")
        relocate("org.yaml", "wiremock.org.yaml")
        relocate("com.ethlo", "wiremock.com.ethlo")
        relocate("com.networknt", "wiremock.com.networknt")

        mergeServiceFiles()
    }
}

signing {
    // Docs: https://github.com/wiremock/community/blob/main/infra/maven-central.md
    val isPublishTask = gradle.taskGraph.hasTask("uploadArchives") || gradle.taskGraph.hasTask("publish")
    logger.info("Checking signing requirements: Version: $version, Publish: $isPublishTask")
    val required = !version.toString().contains("SNAPSHOT") && isPublishTask

    val signingKey = providers.environmentVariable("OSSRH_GPG_SECRET_KEY").orElse("").get()
    val signingPassphrase = providers.environmentVariable("OSSRH_GPG_SECRET_KEY_PASSWORD").orElse("").get()
    if (signingKey.isNotEmpty() && signingPassphrase.isNotEmpty()) {
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign(publishing.publications)
    } else {
        if (required) {
            logger.error("Will not be signing the artifacts, no signing key or password specified via environment variables")
            throw GradleException("Cannot sign artifacts")
        } else {
            logger.info("Skipping signing of the artifacts, not required")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/wiremock/${githubRepo}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    getComponents().withType(AdhocComponentWithVariants).each { c ->
        c.withVariantsFromConfiguration(project.configurations.shadowRuntimeElements) {
            skip()
        }
    }

    publications {
        publishing {
            dependencies.add("checkReleasePreconditions")
            tasks.publish.dependsOn("signStandaloneJarPublication")
            tasks.publish.dependsOn("signMavenJavaPublication")
        register("mavenJava", MavenPublication::class) {
            artifactId = baseArtifact
            from(components["java"])

                    pom {
                        name = baseArtifact
                        description = project.description
                        url = "https://github.com/wiremock/${githubRepo}"


                        scm {
                            connection = "https://github.com/wiremock/${githubRepo}.git"
                            developerConnection = "https://github.com/wiremock/${githubRepo}.git"
                            url = "https://github.com/wiremock/${githubRepo}.git"
                        }

                        licenses {
                            license {
                                name = "The Apache Software License, Version 2.0"
                                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                                distribution = "repo"
                            }
                        }

                        developers {
                            developer {
                                id = project.findProperty("developer.id").toString() ?: "wiremock-build-bot"
                                name = project.findProperty("developer.name").toString() ?: "WireMock Build Bot"
                                email = project.findProperty("developer.email").toString() ?: "build-bot@wiremock.org"
                            }
                        }
                    }
        }

        register("mavenJava", MavenPublication::class) {
            artifactId = "$baseArtifact-standalone"

            project.shadow.component(publication)

            artifact sourcesJar
                    artifact javadocJar

                    pom {

                        name = "${baseArtifact}-standalone"
                        description = "${description}"
                        url = "https://github.com/wiremock/${githubRepo}"

                        scm {
                            connection = "https://github.com/wiremock/${githubRepo}.git"
                            developerConnection = "https://github.com/wiremock/${githubRepo}.git"
                            url = "https://github.com/wiremock/${githubRepo}.git"
                        }

                        licenses {
                            license {
                                name = "The Apache Software License, Version 2.0"
                                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                                distribution = "repo"
                            }
                        }

                        developers {
                            developer {
                                id = project.findProperty("developer.id").toString() ?: "wiremock-build-bot"
                                name = project.findProperty("developer.name").toString() ?: "WireMock Build Bot"
                                email = project.findProperty("developer.email").toString() ?: "build-bot@wiremock.org"
                            }
                        }
                    }
        }}
    }
}

tasks.register("checkReleasePreconditions") {
    doLast {
        val REQUIRED_GIT_BRANCH = "HEAD"
        val currentGitBranch = "git rev-parse --abbrev-ref HEAD".execute().text.trim()
        // FIXME: assert currentGitBranch == REQUIRED_GIT_BRANCH, "Must be on the $REQUIRED_GIT_BRANCH branch in order to release to Sonatype"
    }
}


// FIXME: remove after https://github.com/gradle/gradle/issues/26091
tasks.withType(AbstractPublishToMaven).configureEach {
    val signingTasks = tasks.withType(Sign)
    mustRunAfter(signingTasks)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    shadowed("org.wiremock:wiremock:${versions.wiremock}")

    testImplementation("org.wiremock:wiremock:${versions.wiremock}")
    testImplementation(platform("org.junit:junit-bom:${versions.junit}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${versions.assertj}")
    testImplementation(platform("io.rest-assured:rest-assured-bom:${versions.restAssured}"))
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.awaitility:awaitility:" + versions.awaitility)
    testImplementation("org.testcontainers:junit-jupiter:${versions.testcontainers}")
    testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:${versions.wiremockTestcontainers}")
}


sourceCompatibility = 11
targetCompatibility = 11

compileJava {
    options.encoding = 'UTF-8'
}

compileTestJava {
    options.encoding = 'UTF-8'
}
assemble.dependsOn("jar", "shadowJar")
test.dependsOn("shadowJar")

test {
    useJUnitPlatform()
    testLogging {
        events = arrayListOf("passed", "skipped", "failed")
    }
}

idea {
    project {
        settings {
            jdkName = "11"
            languageLevel = org.gradle.plugins.ide.idea.model.IdeaLanguageLevel("11")
        }
    }
}

nexusPublishing {
    // See https://github.com/wiremock/community/blob/main/infra/maven-central.md
    repositories {
        sonatype {
            // TODO: allow configuring destinations for oss1
            // nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            // snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            val envUsername = providers.environmentVariable("OSSRH_USERNAME").orElse("").get()
            val envPassword = providers.environmentVariable("OSSRH_TOKEN").orElse("").get()
            if (!envUsername.isEmpty() && !envPassword.isEmpty()) {
                username = envUsername
                password = envPassword
            }
        }
    }
}
