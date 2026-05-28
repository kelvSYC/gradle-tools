package com.kelvsyc.gradle.aws.jvm.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class AwsLambdaJvmFatPackagePluginSpec : FunSpec() {
    init {
        test("apply with java plugin - registers lambdaFatJar and lambdaDeploymentZip tasks") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmFatPackagePlugin::class.java)

            project.tasks.findByName("lambdaFatJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
        }

        test("apply - creates awsLambdaJvm extension with project name as archiveBaseName default") {
            val project = ProjectBuilder.builder().withName("my-lambda").build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmFatPackagePlugin::class.java)

            val extension = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            extension.shouldNotBeNull()
            extension.archiveBaseName.get() shouldBe "my-lambda"
        }

        test("apply without java plugin - defers task registration until java is applied") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AwsLambdaJvmFatPackagePlugin::class.java)

            project.tasks.findByName("lambdaFatJar") shouldBe null
            project.tasks.findByName("lambdaDeploymentZip") shouldBe null

            project.pluginManager.apply("java")

            project.tasks.findByName("lambdaFatJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
        }
    }
}
