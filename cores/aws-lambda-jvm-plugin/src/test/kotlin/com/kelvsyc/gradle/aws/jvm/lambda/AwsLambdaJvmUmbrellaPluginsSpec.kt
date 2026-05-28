package com.kelvsyc.gradle.aws.jvm.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class AwsLambdaJvmUmbrellaPluginsSpec : FunSpec() {
    init {
        test("AwsLambdaJvmPlugin - applies runtime and fat-package plugins") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmPlugin::class.java)

            val deps = project.configurations.getByName("implementation").dependencies
            deps.any { it.name == "aws-lambda-java-core" }.shouldBeTrue()
            project.tasks.findByName("lambdaFatJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
        }

        test("AwsLambdaJvmLayeredPlugin - applies runtime and thin-package plugins") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmLayeredPlugin::class.java)

            val deps = project.configurations.getByName("implementation").dependencies
            deps.any { it.name == "aws-lambda-java-core" }.shouldBeTrue()
            project.tasks.findByName("lambdaJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
        }

        test("AwsLambdaJvmPlugin - extension deploymentZipFile is set after java plugin applied") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmPlugin::class.java)

            val extension = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            extension.shouldNotBeNull()
            extension.deploymentZipFile.isPresent.shouldBeTrue()
        }
    }
}
