package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.sdk.kotlin.services.codeartifact.model.PublishPackageVersionRequest
import aws.sdk.kotlin.services.codeartifact.model.PublishPackageVersionResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class PublishPackageVersionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to CodeArtifact") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val requestSlot = slot<PublishPackageVersionRequest>()
            coEvery { client.publishPackageVersion(capture(requestSlot)) } returns PublishPackageVersionResponse {}

            val assetFile = Files.createTempFile("publish-test", ".jar")
            Files.writeString(assetFile, "test-content")

            val task = project.tasks.create("publishPackageVersion", PublishPackageVersion::class.java)
            task.service.set(service)
            task.domain.set("my-domain")
            task.domainOwner.set("123456789012")
            task.repository.set("my-repo")
            task.namespace.set("my-namespace")
            task.packageValue.set("my-package")
            task.packageVersion.set("1.0.0")
            task.assetName.set("my-asset.jar")
            task.assetSHA256.set("abc123def456")
            task.assetContent.set(assetFile.toFile())
            task.execute()

            val captured = requestSlot.captured
            captured.domain shouldBe "my-domain"
            captured.domainOwner shouldBe "123456789012"
            captured.repository shouldBe "my-repo"
            captured.format shouldBe PackageFormat.Generic
            captured.namespace shouldBe "my-namespace"
            captured.`package` shouldBe "my-package"
            captured.packageVersion shouldBe "1.0.0"
            captured.assetName shouldBe "my-asset.jar"
            captured.assetSha256 shouldBe "abc123def456"

            assetFile.toFile().delete()
        }

        test("execute - includes unfinished=true when property is set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca2", MockCodeArtifactClientBuildService::class)
            val requestSlot = slot<PublishPackageVersionRequest>()
            coEvery { client.publishPackageVersion(capture(requestSlot)) } returns PublishPackageVersionResponse {}

            val assetFile = Files.createTempFile("publish-test", ".jar")
            Files.writeString(assetFile, "test-content")

            val task = project.tasks.create("publishPackageVersion", PublishPackageVersion::class.java)
            task.service.set(service)
            task.domain.set("my-domain")
            task.domainOwner.set("123456789012")
            task.repository.set("my-repo")
            task.namespace.set("my-namespace")
            task.packageValue.set("my-package")
            task.packageVersion.set("1.0.0")
            task.assetName.set("my-asset.jar")
            task.assetSHA256.set("abc123def456")
            task.assetContent.set(assetFile.toFile())
            task.unfinished.set(true)
            task.execute()

            val captured = requestSlot.captured
            captured.unfinished shouldBe true

            assetFile.toFile().delete()
        }

        test("execute - omits unfinished when property is not set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca3", MockCodeArtifactClientBuildService::class)
            val requestSlot = slot<PublishPackageVersionRequest>()
            coEvery { client.publishPackageVersion(capture(requestSlot)) } returns PublishPackageVersionResponse {}

            val assetFile = Files.createTempFile("publish-test", ".jar")
            Files.writeString(assetFile, "test-content")

            val task = project.tasks.create("publishPackageVersion", PublishPackageVersion::class.java)
            task.service.set(service)
            task.domain.set("my-domain")
            task.domainOwner.set("123456789012")
            task.repository.set("my-repo")
            task.namespace.set("my-namespace")
            task.packageValue.set("my-package")
            task.packageVersion.set("1.0.0")
            task.assetName.set("my-asset.jar")
            task.assetSHA256.set("abc123def456")
            task.assetContent.set(assetFile.toFile())
            task.execute()

            val captured = requestSlot.captured
            captured.unfinished shouldBe null

            assetFile.toFile().delete()
        }
    }
}
