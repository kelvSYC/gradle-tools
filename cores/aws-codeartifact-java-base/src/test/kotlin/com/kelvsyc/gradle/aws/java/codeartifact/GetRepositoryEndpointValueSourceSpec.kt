package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.codeartifact.MockCodeArtifactClientInfoInternal
import com.kelvsyc.gradle.plugins.CodeArtifactJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.EndpointType
import software.amazon.awssdk.services.codeartifact.model.GetRepositoryEndpointRequest
import software.amazon.awssdk.services.codeartifact.model.GetRepositoryEndpointResponse
import software.amazon.awssdk.services.codeartifact.model.PackageFormat

class GetRepositoryEndpointValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns repository endpoint with default format and endpointType") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val slot = slot<GetRepositoryEndpointRequest>()
            val response = mockk<GetRepositoryEndpointResponse>()
            every { response.repositoryEndpoint() } returns "https://example.codeartifact.amazonaws.com/"
            every { client.getRepositoryEndpoint(capture(slot)) } returns response

            val provider = project.providers.of(GetRepositoryEndpointValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
            }

            provider.get() shouldBe "https://example.codeartifact.amazonaws.com/"
            slot.captured.domain() shouldBe "my-domain"
            slot.captured.domainOwner() shouldBe "123456789012"
            slot.captured.repository() shouldBe "my-repo"
            slot.captured.endpointType() shouldBe EndpointType.IPV4
            slot.captured.format() shouldBe PackageFormat.GENERIC
        }

        test("obtain - uses provided endpointType and format") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val slot = slot<GetRepositoryEndpointRequest>()
            val response = mockk<GetRepositoryEndpointResponse>()
            every { response.repositoryEndpoint() } returns "https://example.codeartifact.amazonaws.com/"
            every { client.getRepositoryEndpoint(capture(slot)) } returns response

            val provider = project.providers.of(GetRepositoryEndpointValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.repository.set("my-repo")
                parameters.endpointType.set(EndpointType.DUALSTACK)
                parameters.format.set(PackageFormat.MAVEN)
            }

            provider.get() shouldBe "https://example.codeartifact.amazonaws.com/"
            slot.captured.endpointType() shouldBe EndpointType.DUALSTACK
            slot.captured.format() shouldBe PackageFormat.MAVEN
        }

        test("obtain - returns null on CodeartifactException") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            every { client.getRepositoryEndpoint(any<GetRepositoryEndpointRequest>()) } throws
                CodeartifactException.builder().message("Not found").build()

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
