package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GetGenericPackageVersionAssetSpec : FunSpec() {
    init {
        test("execute - passes correct request parameters to CodeArtifact") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val requestSlot = slot<GetPackageVersionAssetRequest>()
            coEvery { client.getPackageVersionAsset<Unit>(capture(requestSlot), any()) } returns Unit

            val outputFile = Files.createTempFile("asset-test", ".zip")

            val task = project.tasks.create("getAsset", GetGenericPackageVersionAsset::class.java)
            task.service.set(service)
            task.domain.set("my-domain")
            task.domainOwner.set("123456789012")
            task.repository.set("my-repo")
            task.namespace.set("my-namespace")
            task.packageValue.set("my-package")
            task.packageVersion.set("1.0.0")
            task.asset.set("my-asset.zip")
            task.outputFile.set(outputFile.toFile())
            task.execute()

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
