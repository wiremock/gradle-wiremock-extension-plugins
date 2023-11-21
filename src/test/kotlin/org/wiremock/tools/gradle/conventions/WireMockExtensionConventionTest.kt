package org.wiremock.tools.gradle.conventions

import org.gradle.kotlin.dsl.embeddedKotlinVersion

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertThat
import org.junit.Test

class WireMockExtensionConventionTest {
    @Rule
    @JvmField
    val tmpDir = TemporaryFolder()

    private val projectDir: File
        get() = tmpDir.root

    private val BASE_SCRIPT : String
        get() =
            """
            buildscript {
                repositories {
                    mavenCentral()
                    mavenLocal()
                }
            }
                  
            plugins {
                kotlin("jvm") version "$embeddedKotlinVersion"
                id("wiremock-extension-convention")
            }
            
            """.trimIndent()

    @Before
    fun setup() {
        withSettings()
        withProperties(
            """
            baseArtifact = my-test-wiremock-extension
            version = 1.0.0-SNAPSHOT
            description = My Test WireMock Extension
            githubRepo = wiremock-my-test-extension 
            """.trimIndent()
        )
        withBuildScript(BASE_SCRIPT)
        withFile("src/main/java/Test.java",
            """
            public class Test
            {
                public static void main(String []args)
                {
                    System.out.println("My First Java Program.");
                }
            };    
            """.trimIndent())
    }

    @Test
    fun smokeTest() {
        assertThat(
                build("build", "-s").output,
                containsString("shadowJar")
        )
    }

    @Test
    fun shading() {
        withBuildScript(BASE_SCRIPT +
                """
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
                """.trimIndent()
        )
        assertThat(
                build("build", "-s").output,
                containsString("shadowJar").also { containsString("wiremock.com.github.jknack") }
        )
    }


    private
    fun build(vararg arguments: String) =
            gradleRunnerFor(*arguments).build()

    private
    fun buildAndFail(vararg arguments: String) =
            gradleRunnerFor(*arguments).buildAndFail()

    private
    fun gradleRunnerFor(vararg arguments: String) =
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withPluginClasspath()
                    .withArguments(*arguments)

    private
    fun withBuildScript(text: String) =
            withFile("build.gradle.kts", text)

    private
    fun withSettings(text: String = "") =
            withFile("settings.gradle.kts", text)

    private
    fun withProperties(text: String = "") =
            withFile("gradle.properties", text)

    private
    fun withSource(text: String) =
            withFile("src/main/kotlin/source.kt", text)

    private
    fun withFile(path: String, text: String) =
            projectDir.resolve(path).apply {
                parentFile.mkdirs()
                writeText(text.trimIndent())
            }

    private
    fun BuildResult.outcomeOf(taskPath: String): TaskOutcome? =
            task(taskPath)?.outcome

    private
    fun String.withoutAnsiColorCodes() =
            replace(Regex("\u001B\\[[;\\d]*m"), "")

}