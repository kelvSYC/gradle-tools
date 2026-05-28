package com.kelvsyc.gradle.aws.jvm.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import org.gradle.testfixtures.ProjectBuilder

class AwsLambdaJvmRuntimePluginSpec : FunSpec() {
    init {
        test("apply - adds aws-lambda-java-core to implementation configuration") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("java")

            project.pluginManager.apply(AwsLambdaJvmRuntimePlugin::class.java)

            val deps = project.configurations.getByName("implementation").dependencies
            deps.any { dep ->
                dep.group == "com.amazonaws" &&
                    dep.name == "aws-lambda-java-core" &&
                    dep.version == AwsLambdaJvmRuntimePlugin.LAMBDA_CORE_VERSION
            }.shouldBeTrue()
        }
    }
}
