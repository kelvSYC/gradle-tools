package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.ListPackageVersionsRequest
import aws.sdk.kotlin.services.codeartifact.model.ListPackageVersionsResponse
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.sdk.kotlin.services.codeartifact.model.PackageVersionStatus
import aws.sdk.kotlin.services.codeartifact.model.PackageVersionSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListPackageVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns list of version strings") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val slot = slot<ListPackageVersionsRequest>()
            coEvery { client.listPackageVersions(capture(slot)) } returns ListPackageVersionsResponse {
                versions = listOf(
                    PackageVersionSummary { version = "1.0.0"; status = PackageVersionStatus.Published },
                    PackageVersionSummary { version = "2.0.0"; status = PackageVersionStatus.Published },
                )
            }

            val provider = project.providers.ofKt(ListPackageVersionsValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.format.set("generic")
                parameters.namespace.set("my-namespace")
                parameters.packageValue.set("my-package")
            }

            provider.get() shouldBe listOf("1.0.0", "2.0.0")
            slot.captured.domain shouldBe "my-domain"
            slot.captured.domainOwner shouldBe "123456789012"
            slot.captured.repository shouldBe "my-repo"
            slot.captured.format shouldBe PackageFormat.Generic
            slot.captured.namespace shouldBe "my-namespace"
            slot.captured.`package` shouldBe "my-package"
        }
    }
}
