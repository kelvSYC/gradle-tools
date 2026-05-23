package com.kelvsyc.gradle.aws.java.codeartifact

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.PackageFormat
import java.nio.file.Files

class AbstractBatchPublishPackageVersionSpec : FunSpec() {
    init {
        test("Register single artifact - configures request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "codeartifact",
                MockCodeArtifactClientBuildService::class,
            )
            val assetFile = Files.createTempFile("asset", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractWorkerBatchPublishPackageVersion>("myTask") {
                    this.service.set(service)
                    registerArtifact("artifact1") {
                        it.domain.set("my-domain")
                        it.domainOwner.set("123456789012")
                        it.repository.set("my-repo")
                        it.namespace.set("my-namespace")
                        it.packageValue.set("my-package")
                        it.packageVersion.set("1.0.0")
                        it.assetName.set("my-asset.zip")
                        it.assetSHA256.set("abc123")
                        it.assetContent.set(assetFile)
                    }
                }

                val reqs = task.get().requests.get()
                reqs shouldHaveSize 1
                reqs[0].name shouldBe "artifact1"
                reqs[0].request.domain() shouldBe "my-domain"
                reqs[0].request.domainOwner() shouldBe "123456789012"
                reqs[0].request.repository() shouldBe "my-repo"
                reqs[0].request.format() shouldBe PackageFormat.GENERIC
                reqs[0].request.namespace() shouldBe "my-namespace"
                reqs[0].request.packageValue() shouldBe "my-package"
                reqs[0].request.packageVersion() shouldBe "1.0.0"
                reqs[0].request.assetName() shouldBe "my-asset.zip"
                reqs[0].request.assetSHA256() shouldBe "abc123"
                reqs[0].request.unfinished() shouldBe null
            } finally {
                assetFile.delete()
                MockCodeArtifactClientBuildService.mockClient = null
            }
        }

        test("Register artifact with unfinished flag - includes flag in request") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "codeartifact",
                MockCodeArtifactClientBuildService::class,
            )
            val assetFile = Files.createTempFile("asset", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractWorkerBatchPublishPackageVersion>("myTask") {
                    this.service.set(service)
                    registerArtifact("artifact1") {
                        it.domain.set("my-domain")
                        it.domainOwner.set("123456789012")
                        it.repository.set("my-repo")
                        it.namespace.set("my-namespace")
                        it.packageValue.set("my-package")
                        it.packageVersion.set("1.0.0")
                        it.assetName.set("my-asset.zip")
                        it.assetSHA256.set("abc123")
                        it.assetContent.set(assetFile)
                        it.unfinished.set(true)
                    }
                }

                val reqs = task.get().requests.get()
                reqs shouldHaveSize 1
                reqs[0].request.unfinished() shouldBe true
            } finally {
                assetFile.delete()
                MockCodeArtifactClientBuildService.mockClient = null
            }
        }

        test("Register multiple artifacts - configures all request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "codeartifact",
                MockCodeArtifactClientBuildService::class,
            )
            val assetFile1 = Files.createTempFile("asset1", ".zip").toFile()
            val assetFile2 = Files.createTempFile("asset2", ".zip").toFile()

            try {
                val task = project.tasks.register<AbstractWorkerBatchPublishPackageVersion>("myTask") {
                    this.service.set(service)
                    registerArtifact("artifact1") {
                        it.domain.set("my-domain")
                        it.domainOwner.set("123456789012")
                        it.repository.set("my-repo")
                        it.namespace.set("ns1")
                        it.packageValue.set("pkg1")
                        it.packageVersion.set("1.0.0")
                        it.assetName.set("asset1.zip")
                        it.assetSHA256.set("sha1")
                        it.assetContent.set(assetFile1)
                        it.unfinished.set(true)
                    }
                    registerArtifact("artifact2") {
                        it.domain.set("my-domain")
                        it.domainOwner.set("123456789012")
                        it.repository.set("my-repo")
                        it.namespace.set("ns1")
                        it.packageValue.set("pkg1")
                        it.packageVersion.set("1.0.0")
                        it.assetName.set("asset2.zip")
                        it.assetSHA256.set("sha2")
                        it.assetContent.set(assetFile2)
                    }
                }

                val artifacts = task.get().artifacts.get()
                val reqs = task.get().requests.get()
                reqs shouldHaveSize artifacts.size
                artifacts.forEach { (artifactName, artifact) ->
                    reqs.any {
                        it.name == artifactName &&
                            it.request.assetName() == artifact.assetName.orNull
                    }.shouldBeTrue()
                }
            } finally {
                assetFile1.delete()
                assetFile2.delete()
                MockCodeArtifactClientBuildService.mockClient = null
            }
        }
    }
}
