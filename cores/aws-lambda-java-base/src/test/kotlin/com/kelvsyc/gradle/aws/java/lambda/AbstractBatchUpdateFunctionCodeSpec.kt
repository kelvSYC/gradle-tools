package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import java.nio.file.Files

class AbstractBatchUpdateFunctionCodeSpec : FunSpec() {
    init {
        test("Register single artifact - configures request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "lambda",
                MockLambdaClientBuildService::class,
            )
            val zipFile = Files.createTempFile("fn", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractSyncBatchUpdateFunctionCode>("myTask") {
                    this.service.set(service)
                    registerArtifact("fn1") {
                        it.functionName.set("my-fn")
                        it.zipFile.set(zipFile)
                        it.publish.set(true)
                    }
                }

                val reqs = task.get().requests.get()
                reqs shouldHaveSize 1
                reqs[0].name shouldBe "fn1"
                reqs[0].functionName shouldBe "my-fn"
                reqs[0].publish shouldBe true
            } finally {
                zipFile.delete()
                MockLambdaClientBuildService.mockClient = null
            }
        }

        test("Register artifact without publish flag - publish is null") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "lambda",
                MockLambdaClientBuildService::class,
            )
            val zipFile = Files.createTempFile("fn", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractSyncBatchUpdateFunctionCode>("myTask") {
                    this.service.set(service)
                    registerArtifact("fn1") {
                        it.functionName.set("my-fn")
                        it.zipFile.set(zipFile)
                    }
                }

                val reqs = task.get().requests.get()
                reqs shouldHaveSize 1
                reqs[0].publish.shouldBeNull()
            } finally {
                zipFile.delete()
                MockLambdaClientBuildService.mockClient = null
            }
        }

        test("Register multiple artifacts - all artifacts appear in requests") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "lambda",
                MockLambdaClientBuildService::class,
            )
            val zipFile1 = Files.createTempFile("fn1", ".zip").toFile()
            val zipFile2 = Files.createTempFile("fn2", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractSyncBatchUpdateFunctionCode>("myTask") {
                    this.service.set(service)
                    registerArtifact("fn1") {
                        it.functionName.set("my-fn-1")
                        it.zipFile.set(zipFile1)
                        it.publish.set(true)
                    }
                    registerArtifact("fn2") {
                        it.functionName.set("my-fn-2")
                        it.zipFile.set(zipFile2)
                    }
                }

                val artifacts = task.get().artifacts.get()
                val reqs = task.get().requests.get()
                reqs shouldHaveSize artifacts.size
                artifacts.forEach { (artifactName, artifact) ->
                    reqs.any {
                        it.name == artifactName &&
                            it.functionName == artifact.functionName.orNull
                    }.shouldBeTrue()
                }
            } finally {
                zipFile1.delete()
                zipFile2.delete()
                MockLambdaClientBuildService.mockClient = null
            }
        }
    }
}
