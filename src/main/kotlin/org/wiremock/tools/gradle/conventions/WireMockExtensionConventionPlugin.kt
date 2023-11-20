package org.wiremock.tools.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.plugins.JavaPluginExtension

import org.gradle.cache.internal.GeneratedGradleJarCache
import org.gradle.jvm.toolchain.JavaLanguageVersion

import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.plugins.signing.Sign
import java.util.concurrent.Callable

public
class WireMockExtensionConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.gradle.java-library")
        project.pluginManager.apply("org.gradle.maven-publish")
        project.pluginManager.apply("io.github.gradle-nexus.publish-plugin")

        project.extensions.configure<JavaPluginExtension>("java") {
            withJavadocJar()
            withSourcesJar()
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(11))
            }
        }

        // Publishing destinations
        project.repositories.mavenCentral()
        project.repositories.mavenLocal()
        project.repositories.maven {
            name = "GitHubPackages"
            url = "https://maven.pkg.github.com/wiremock/wiremock-state-extension"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        //TODO: Add Maven Central
        project.components.withType<AdhocComponentWithVariants>().each { c ->
            c.withVariantsFromConfiguration(project.configurations.shadowRuntimeElements) {
                skip()
            }
        }

        project.tasks.withType<Sign>().configureEach {
            // Docs: https://github.com/wiremock/community/blob/main/infra/maven-central.md
            required {
                !version.toString().contains("SNAPSHOT") && (gradle.taskGraph.hasTask("uploadArchives") || gradle.taskGraph.hasTask("publish"))
            }
            val signingKey = providers.environmentVariable("OSSRH_GPG_SECRET_KEY").orElse("").get()
            val  signingPassphrase = providers.environmentVariable("OSSRH_GPG_SECRET_KEY_PASSWORD").orElse("").get()
            if (!signingKey.isEmpty() && !signingPassphrase.isEmpty()) {
                useInMemoryPgpKeys(signingKey, signingPassphrase)
                sign(publishing.publications)
            }
        }

        publishing {


            getComponents().withType(AdhocComponentWithVariants).each { c ->

            }


            publications {
                create<MavenPublication> {
                    artifactId = "${baseArtifact}"

                    from components.java

                    pom {
                        name = "${baseArtifact}"
                        description = 'A WireMock extension to transfer state in between stubs'
                        url = 'https://github.com/wiremock/wiremock-state-extension'


                        scm {
                            connection = 'https://github.com/wiremock/wiremock-state-extension.git'
                            developerConnection = 'https://github.com/wiremock/wiremock-state-extension.git'
                            url = 'https://github.com/wiremock/wiremock-state-extension.git'
                        }

                        licenses {
                            license {
                                name = 'The Apache Software License, Version 2.0'
                                url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                distribution = 'repo'
                            }
                        }

                        developers {
                            developer {
                                id = 'dirkbolte'
                                name = 'Dirk Bolte'
                                email = 'dirk.bolte@gmx.de'
                            }
                        }
                    }
                }
                standaloneJar(MavenPublication) { publication ->
                    artifactId = "${baseArtifact}-standalone"

                    project.shadow.component(publication)

                    artifact sourcesJar
                    artifact javadocJar

                    pom {

                        name = "${baseArtifact}-standalone"
                        description = 'A WireMock extension to transfer state in between stubs - to be used with WireMock standalone'
                        url = 'https://github.com/wiremock/wiremock-state-extension'


                        scm {
                            connection = 'https://github.com/wiremock/wiremock-state-extension.git'
                            developerConnection = 'https://github.com/wiremock/wiremock-state-extension.git'
                            url = 'https://github.com/wiremock/wiremock-state-extension.git'
                        }

                        licenses {
                            license {
                                name = 'The Apache Software License, Version 2.0'
                                url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                distribution = 'repo'
                            }
                        }

                        developers {
                            developer {
                                id = 'dirkbolte'
                                name = 'Dirk Bolte'
                                email = 'dirk.bolte@gmx.de'
                            }
                        }
                    }
                }
            }
        }

    }

}
