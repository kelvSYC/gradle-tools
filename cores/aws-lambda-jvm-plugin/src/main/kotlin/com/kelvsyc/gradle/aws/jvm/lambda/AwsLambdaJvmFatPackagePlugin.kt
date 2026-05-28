package com.kelvsyc.gradle.aws.jvm.lambda

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

/**
 * Registers tasks that package a JVM Lambda function as a fat JAR.
 *
 * When applied alongside a JVM language plugin (e.g. `java`, `kotlin("jvm")`), this plugin
 * registers two tasks:
 * - `lambdaFatJar` — an all-in-one JAR containing the project's own classes merged with all
 *   runtime dependencies. Duplicate entries are suppressed; signature files are stripped to
 *   prevent JAR signature verification failures at Lambda runtime.
 * - `lambdaDeploymentZip` — wraps the fat JAR in a ZIP suitable for uploading to AWS Lambda.
 *
 * The path to the generated ZIP is published to [AwsLambdaJvmExtension.deploymentZipFile] for
 * downstream consumption by the upload plugin.
 *
 * This plugin is applied automatically by [AwsLambdaJvmPlugin]. Apply it directly when you want
 * fat-JAR packaging without the `aws-lambda-java-core` runtime dependency.
 */
abstract class AwsLambdaJvmFatPackagePlugin @Inject constructor(
    private val archiveOperations: ArchiveOperations,
) : Plugin<Project> {
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

        val fatJar = project.tasks.register<Jar>("lambdaFatJar") {
            archiveBaseName.convention(extension.archiveBaseName)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(mainOutput)
            from(runtimeClasspath.map { config ->
                config.files.map { archiveOperations.zipTree(it) }
            })
            exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/LICENSE*")
        }

        val zip = project.tasks.register<Zip>("lambdaDeploymentZip") {
            archiveBaseName.convention(extension.archiveBaseName)
            from(fatJar)
        }

        extension.deploymentZipFile.set(zip.flatMap { it.archiveFile })
    }
}
