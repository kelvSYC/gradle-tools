package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetFileRequest
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.artifact.MockArtifactRegistryClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudArtifactRegistryBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import java.io.InputStream

class AbstractArtifactValueSourceSpec : FunSpec() {
    abstract class ConcreteArtifactValueSource :
        AbstractArtifactValueSource<String, AbstractArtifactValueSource.Parameters>() {
        override fun doObtain(input: InputStream): String = "obtained"
    }

    init {
        test("obtain - calls getFile with request built from parameters") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val slot = slot<GetFileRequest>()
            val mockFile = mockk<com.google.devtools.artifactregistry.v1.File>(relaxed = true)
            every { client.getFile(capture(slot)) } returns mockFile

            val provider = project.providers.of(ConcreteArtifactValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectName.set("my-project")
                parameters.location.set("us-central1")
                parameters.repository.set("my-repo")
                parameters.filename.set("file.txt")
            }

            val result = provider.get()

            result shouldBe "obtained"
            verify { client.getFile(any<GetFileRequest>()) }
        }

        test("obtain - returns result of doObtain") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val mockFile = mockk<com.google.devtools.artifactregistry.v1.File>(relaxed = true)
            every { client.getFile(any<GetFileRequest>()) } returns mockFile

            val provider = project.providers.of(ConcreteArtifactValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectName.set("my-project")
                parameters.location.set("us-central1")
                parameters.repository.set("my-repo")
                parameters.filename.set("file.txt")
            }

            provider.get() shouldBe "obtained"
        }
    }
}
