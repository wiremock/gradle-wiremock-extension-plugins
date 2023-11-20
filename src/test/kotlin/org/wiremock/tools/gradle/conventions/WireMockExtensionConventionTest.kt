package org.wiremock.tools.gradle.conventions

import org.gradle.kotlin.dsl.embeddedKotlinVersion

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat

class WireMockExtensionConventionTest {
    @Rule
    @JvmField
    val tmpDir = TemporaryFolder()

    private
    val projectDir: File
        get() = tmpDir.root

    @Before
    fun setup() {
        withSettings()
        withBuildScript(
                """
            plugins {
                kotlin("jvm") version "$embeddedKotlinVersion"
                id("org.wiremock.tools.gradle.wiremock-extension-convention")
            }

            """
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
    val ktlintReportFile: File by lazy { projectDir.resolve("build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.txt") }

    private
    fun assertKtlintErrors(count: Int) =
            assertThat(
                    "ktlint error count in\n${ktlintReportFile.readText()}",
                    ktlintReportFile.readLines().filter { it.contains("source.kt") }.count(),
                    equalTo(count)
            )

    private
    fun assertKtLintError(error: String, line: Int, column: Int) =
            assertThat(
                    ktlintReportFile.readText().withoutAnsiColorCodes(),
                    containsString("source.kt:$line:$column: $error")
            )

    private
    fun String.withoutAnsiColorCodes() =
            replace(Regex("\u001B\\[[;\\d]*m"), "")

}