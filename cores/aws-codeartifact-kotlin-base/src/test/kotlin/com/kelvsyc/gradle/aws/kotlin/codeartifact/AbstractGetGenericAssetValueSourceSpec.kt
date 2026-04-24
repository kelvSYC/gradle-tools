package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetResponse
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.codeartifact.MockCodeArtifactClientInfoInternal
import com.kelvsyc.gradle.plugins.CodeArtifactKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractGetGenericAssetValueSourceSpec : FunSpec() {
    abstract class StringAssetValueSource :
        AbstractGetGenericAssetValueSource<String, AbstractGetGenericAssetValueSource.Parameters>() {
        override fun doObtain(response: GetPackageVersionAssetResponse): String = "asset-content"
    }

    init {
        test("obtain - passes correct request parameters to CodeArtifact and delegates to doObtain") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val requestSlot = slot<GetPackageVersionAssetRequest>()
            coEvery { client.getPackageVersionAsset<String>(capture(requestSlot), any()) } returns "asset-content"

            val provider = project.providers.of(StringAssetValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
