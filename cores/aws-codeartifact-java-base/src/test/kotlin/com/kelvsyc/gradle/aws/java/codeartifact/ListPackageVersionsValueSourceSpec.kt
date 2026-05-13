package com.kelvsyc.gradle.aws.java.codeartifact

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.ListPackageVersionsRequest
import software.amazon.awssdk.services.codeartifact.model.ListPackageVersionsResponse
import software.amazon.awssdk.services.codeartifact.model.PackageFormat
import software.amazon.awssdk.services.codeartifact.model.PackageVersionSummary

class ListPackageVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns list of version strings") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("codeartifact", MockCodeArtifactClientBuildService::class)
            val slot = slot<ListPackageVersionsRequest>()
            val response = ListPackageVersionsResponse.builder()
                .versions(
                    PackageVersionSummary.builder().version("1.0.0").build(),
                    PackageVersionSummary.builder().version("2.0.0").build(),
                )
                .build()
            every { client.listPackageVersions(capture(slot)) } returns response

            val provider = project.providers.ofKt(ListPackageVersionsValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.format.set(PackageFormat.GENERIC)
                parameters.namespace.set("my-namespace")
                parameters.packageValue.set("my-package")
            }

            provider.get() shouldBe listOf("1.0.0", "2.0.0")
            slot.captured.domain() shouldBe "my-domain"
            slot.captured.domainOwner() shouldBe "123456789012"
            slot.captured.repository() shouldBe "my-repo"
            slot.captured.format() shouldBe PackageFormat.GENERIC
            slot.captured.namespace() shouldBe "my-namespace"
            slot.captured.packageValue() shouldBe "my-package"
        }

        test("obtain - returns null on CodeartifactException") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("codeartifact", MockCodeArtifactClientBuildService::class)
            every { client.listPackageVersions(any<ListPackageVersionsRequest>()) } throws
                CodeartifactException.builder().message("Not Found").build()

            val provider = project.providers.ofKt(ListPackageVersionsValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.format.set(PackageFormat.GENERIC)
                parameters.namespace.set("my-namespace")
                parameters.packageValue.set("my-package")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
