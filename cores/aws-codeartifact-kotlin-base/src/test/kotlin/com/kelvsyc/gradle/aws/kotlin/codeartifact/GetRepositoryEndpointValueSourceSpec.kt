package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.EndpointType
import aws.sdk.kotlin.services.codeartifact.model.GetRepositoryEndpointRequest
import aws.sdk.kotlin.services.codeartifact.model.GetRepositoryEndpointResponse
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.codeartifact.MockCodeArtifactClientInfoInternal
import com.kelvsyc.gradle.plugins.CodeArtifactKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetRepositoryEndpointValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns repository endpoint with default format and endpointType") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val slot = slot<GetRepositoryEndpointRequest>()
            val response = mockk<GetRepositoryEndpointResponse>()
            coEvery { response.repositoryEndpoint } returns "https://example.codeartifact.amazonaws.com/"
            coEvery { client.getRepositoryEndpoint(capture(slot)) } returns response

            val provider = project.providers.of(GetRepositoryEndpointValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
            }

            provider.get() shouldBe "https://example.codeartifact.amazonaws.com/"
            slot.captured.domain shouldBe "my-domain"
            slot.captured.domainOwner shouldBe "123456789012"
            slot.captured.repository shouldBe "my-repo"
            slot.captured.endpointType shouldBe EndpointType.Ipv4
            slot.captured.format shouldBe PackageFormat.Generic
        }

        test("obtain - uses provided endpointType and format") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val slot = slot<GetRepositoryEndpointRequest>()
            val response = mockk<GetRepositoryEndpointResponse>()
            coEvery { response.repositoryEndpoint } returns "https://example.codeartifact.amazonaws.com/"
            coEvery { client.getRepositoryEndpoint(capture(slot)) } returns response

            val provider = project.providers.of(GetRepositoryEndpointValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.endpointType.set(EndpointType.Dualstack.value)
                parameters.format.set(PackageFormat.Maven.value)
            }

            provider.get() shouldBe "https://example.codeartifact.amazonaws.com/"
            slot.captured.endpointType shouldBe EndpointType.Dualstack
            slot.captured.format shouldBe PackageFormat.Maven
        }

        test("obtain - returns null when repositoryEndpoint is null") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val response = mockk<GetRepositoryEndpointResponse>()
            coEvery { response.repositoryEndpoint } returns null
            coEvery { client.getRepositoryEndpoint(any()) } returns response

            val provider = project.providers.of(GetRepositoryEndpointValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
