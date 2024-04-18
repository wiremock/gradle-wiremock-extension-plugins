package org.wiremock.tools.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

class WireMockExtensionConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.named("build").also {
            println("Test Extension - WORKS")
        };
    }

}
