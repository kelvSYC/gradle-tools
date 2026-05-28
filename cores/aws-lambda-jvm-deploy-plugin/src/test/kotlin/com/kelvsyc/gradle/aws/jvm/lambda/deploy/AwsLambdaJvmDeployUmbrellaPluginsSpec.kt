package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

class AwsLambdaJvmDeployUmbrellaPluginsSpec : FunSpec() {
    init {
        test("AwsLambdaJvmDeployPlugin - applies fat-package and upload plugins") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmDeployPlugin::class.java)

            val deps = project.configurations.getByName("implementation").dependencies
            deps.any { it.name == "aws-lambda-java-core" }.shouldBeTrue()
            project.tasks.findByName("lambdaFatJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
            project.tasks.findByName("uploadLambdaFunction").shouldNotBeNull()
        }

        test("AwsLambdaJvmLayeredDeployPlugin - applies thin-package and upload plugins") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmLayeredDeployPlugin::class.java)

            val deps = project.configurations.getByName("implementation").dependencies
            deps.any { it.name == "aws-lambda-java-core" }.shouldBeTrue()
            project.tasks.findByName("lambdaJar").shouldNotBeNull()
            project.tasks.findByName("lambdaDeploymentZip").shouldNotBeNull()
            project.tasks.findByName("uploadLambdaFunction").shouldNotBeNull()
        }

        test("AwsLambdaJvmDeployPlugin - deploy extension is wired after java plugin") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmDeployPlugin::class.java)

            project.extensions.findByType(AwsLambdaJvmDeployExtension::class.java).shouldNotBeNull()
        }
    }
}
