package org.wiremock.tools.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

class WireMockExtensionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager){

            }
        }
    }
}
