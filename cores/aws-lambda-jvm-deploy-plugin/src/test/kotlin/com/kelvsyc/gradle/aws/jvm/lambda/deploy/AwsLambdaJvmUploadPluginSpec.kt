package com.kelvsyc.gradle.aws.jvm.lambda.deploy

import com.kelvsyc.gradle.aws.java.lambda.UpdateFunctionCodeTask
import com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmExtension
import com.kelvsyc.gradle.aws.jvm.lambda.AwsLambdaJvmFatPackagePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class AwsLambdaJvmUploadPluginSpec : FunSpec() {
    init {
        test("apply - registers uploadLambdaFunction task") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)

            project.tasks.findByName("uploadLambdaFunction").shouldNotBeNull()
                .shouldBeInstanceOf<UpdateFunctionCodeTask>()
        }

        test("apply - creates awsLambdaJvmDeploy extension") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)

            project.extensions.findByType(AwsLambdaJvmDeployExtension::class.java).shouldNotBeNull()
        }

        test("apply - creates awsLambdaJvm extension with project name default") {
            val project = ProjectBuilder.builder().withName("my-lambda").build()

            project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)

            val ext = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            ext.shouldNotBeNull()
            ext.archiveBaseName.get() shouldBe "my-lambda"
        }

        test("apply - functionName wires from deploy extension to upload task") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)

            val deployExt = project.extensions.getByType(AwsLambdaJvmDeployExtension::class.java)
            deployExt.functionName.set("my-fn")

            val task = project.tasks.getByName("uploadLambdaFunction") as UpdateFunctionCodeTask
            task.functionName.get() shouldBe "my-fn"
        }

        test("apply after fat-package plugin - reuses existing awsLambdaJvm extension") {
            val project = ProjectBuilder.builder().withName("my-lambda").build()
            project.pluginManager.apply("java")
            project.pluginManager.apply(AwsLambdaJvmFatPackagePlugin::class.java)

            project.pluginManager.apply(AwsLambdaJvmUploadPlugin::class.java)

            val ext = project.extensions.findByType(AwsLambdaJvmExtension::class.java)
            ext.shouldNotBeNull()
            ext.archiveBaseName.get() shouldBe "my-lambda"
        }
    }
}
