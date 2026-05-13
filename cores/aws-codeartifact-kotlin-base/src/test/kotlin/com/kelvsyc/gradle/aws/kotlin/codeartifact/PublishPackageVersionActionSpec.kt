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
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class PublishPackageVersionActionSpec : FunSpec() {
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

            val params = project.objects.newInstance<PublishPackageVersionAction.Parameters>()
            params.service.set(service)
            params.domain.set("my-domain")
            params.domainOwner.set("123456789012")
            params.repository.set("my-repo")
            params.namespace.set("my-namespace")
            params.packageValue.set("my-package")
            params.packageVersion.set("1.0.0")
            params.assetName.set("my-asset.jar")
            params.assetSHA256.set("abc123def456")
            params.assetContent.set(assetFile.toFile())

            val action = object : PublishPackageVersionAction() {
                override fun getParameters() = params
            }
            action.execute()

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
    }
}
