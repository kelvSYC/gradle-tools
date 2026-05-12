package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetResponse
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractGetGenericAssetValueSourceSpec : FunSpec() {
    abstract class StringAssetValueSource :
        AbstractGetGenericAssetValueSource<String, AbstractGetGenericAssetValueSource.Parameters>() {
        override fun doObtain(response: GetPackageVersionAssetResponse): String = "asset-content"
    }

    init {
        test("obtain - passes correct request parameters to CodeArtifact and delegates to doObtain") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val requestSlot = slot<GetPackageVersionAssetRequest>()
            coEvery { client.getPackageVersionAsset<String>(capture(requestSlot), any()) } returns "asset-content"

            val provider = project.providers.ofKt(StringAssetValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.namespace.set("my-namespace")
                parameters.packageValue.set("my-package")
                parameters.packageVersion.set("1.0.0")
                parameters.asset.set("my-asset.zip")
            }

            provider.get() shouldBe "asset-content"

            val captured = requestSlot.captured
            captured.domain shouldBe "my-domain"
            captured.domainOwner shouldBe "123456789012"
            captured.repository shouldBe "my-repo"
            captured.format shouldBe PackageFormat.Generic
            captured.namespace shouldBe "my-namespace"
            captured.`package` shouldBe "my-package"
            captured.packageVersion shouldBe "1.0.0"
            captured.asset shouldBe "my-asset.zip"
        }
    }
}
