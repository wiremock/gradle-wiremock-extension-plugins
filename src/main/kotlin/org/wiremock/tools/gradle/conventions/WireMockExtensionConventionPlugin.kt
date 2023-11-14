package org.wiremock.tools.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.cache.internal.GeneratedGradleJarCache

import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf
import java.util.concurrent.Callable

public
class WireMockExtensionConventionPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {

    }

}
