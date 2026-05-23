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

class AbstractBatchGetGenericPackageVersionAssetSpec : FunSpec() {
    init {
        test("Register single artifact - configures request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "codeartifact",
                MockCodeArtifactClientBuildService::class,
            )

            val task = project.tasks.register<AbstractWorkerBatchGetGenericPackageVersionAsset>("myTask") {
                this.service.set(service)
                registerArtifact("artifact1") {
                    it.domain.set("my-domain")
                    it.domainOwner.set("123456789012")
                    it.repository.set("my-repo")
                    it.namespace.set("my-namespace")
                    it.packageValue.set("my-package")
                    it.packageVersion.set("1.0.0")
                    it.assetName.set("my-asset.zip")
                    it.outputFile.set(project.layout.buildDirectory.file("downloads/my-asset.zip"))
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
            reqs[0].request.asset() shouldBe "my-asset.zip"

            MockCodeArtifactClientBuildService.mockClient = null
        }

        test("Register multiple artifacts - configures all request parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "codeartifact",
                MockCodeArtifactClientBuildService::class,
            )

            val task = project.tasks.register<AbstractWorkerBatchGetGenericPackageVersionAsset>("myTask") {
                this.service.set(service)
                registerArtifact("artifact1") {
                    it.domain.set("my-domain")
                    it.domainOwner.set("123456789012")
                    it.repository.set("my-repo")
                    it.namespace.set("ns1")
                    it.packageValue.set("pkg1")
                    it.packageVersion.set("1.0.0")
                    it.assetName.set("asset1.zip")
                    it.outputFile.set(project.layout.buildDirectory.file("downloads/asset1.zip"))
                }
                registerArtifact("artifact2") {
                    it.domain.set("my-domain")
                    it.domainOwner.set("123456789012")
                    it.repository.set("my-repo")
                    it.namespace.set("ns2")
                    it.packageValue.set("pkg2")
                    it.packageVersion.set("2.0.0")
                    it.assetName.set("asset2.zip")
                    it.outputFile.set(project.layout.buildDirectory.file("downloads/asset2.zip"))
                }
            }

            val artifacts = task.get().artifacts.get()
            val reqs = task.get().requests.get()
            reqs shouldHaveSize artifacts.size
            artifacts.forEach { (artifactName, artifact) ->
                reqs.any {
                    it.name == artifactName &&
                        it.request.packageValue() == artifact.packageValue.orNull &&
                        it.request.packageVersion() == artifact.packageVersion.orNull
                }.shouldBeTrue()
            }

            MockCodeArtifactClientBuildService.mockClient = null
        }
    }
}
