package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Registers tasks that package a JVM Lambda function as a thin JAR with a separate `lib/` directory.
 *
 * When applied alongside a JVM language plugin (e.g. `java`, `kotlin("jvm")`), this plugin
 * registers two tasks:
 * - `lambdaJar` — a JAR containing only the project's own classes (no dependencies).
 * - `lambdaDeploymentZip` — a ZIP containing the thin JAR at the root and all runtime
 *   dependencies under `lib/`.
 *
 * This layout suits Lambda deployments backed by a layer that provides the `lib/` dependencies,
 * since the function code ZIP is small and redeploys quickly even when dependencies are large.
 *
 * The path to the generated ZIP is published to [AwsLambdaJvmExtension.deploymentZipFile] for
 * downstream consumption by the upload plugin.
 *
 * This plugin is applied automatically by [AwsLambdaJvmLayeredPlugin]. Apply it directly when you
 * want thin-JAR packaging without the `aws-lambda-java-core` runtime dependency.
 */
class AwsLambdaJvmThinPackagePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            ?: project.extensions.create("awsLambdaJvm", AwsLambdaJvmExtension::class.java).also {
                it.archiveBaseName.convention(project.name)
            }
        project.pluginManager.withPlugin("java") {
            configurePackaging(project, extension)
        }
    }

    private fun configurePackaging(project: Project, extension: AwsLambdaJvmExtension) {
        val mainOutput = project.extensions.getByType<JavaPluginExtension>()
            .sourceSets.named("main").map { it.output }
        val runtimeClasspath = project.configurations.named("runtimeClasspath")

        val jar = project.tasks.register<Jar>("lambdaJar") {
            archiveBaseName.convention(extension.archiveBaseName)
            from(mainOutput)
        }

        val zip = project.tasks.register<Zip>("lambdaDeploymentZip") {
            archiveBaseName.convention(extension.archiveBaseName)
            from(jar)
            into("lib") {
                from(runtimeClasspath)
            }
        }

        extension.deploymentZipFile.set(zip.flatMap { it.archiveFile })
    }
}
