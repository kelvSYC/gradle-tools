package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.codeartifact.MockCodeArtifactClientInfoInternal
import com.kelvsyc.gradle.plugins.CodeArtifactKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GetGenericPackageVersionAssetActionSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to CodeArtifact") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val requestSlot = slot<GetPackageVersionAssetRequest>()
            coEvery { client.getPackageVersionAsset<Unit>(capture(requestSlot), any()) } returns Unit

            val outputFile = Files.createTempFile("asset-test", ".zip")

            val params = project.objects.newInstance<GetGenericPackageVersionAssetAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.domain.set("my-domain")
            params.domainOwner.set("123456789012")
            params.repository.set("my-repo")
            params.namespace.set("my-namespace")
            params.packageValue.set("my-package")
            params.packageVersion.set("1.0.0")
            params.asset.set("my-asset.zip")
            params.outputFile.set(outputFile.toFile())

            val action = object : GetGenericPackageVersionAssetAction() {
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
            captured.asset shouldBe "my-asset.zip"

            outputFile.toFile().delete()
        }
    }
}
