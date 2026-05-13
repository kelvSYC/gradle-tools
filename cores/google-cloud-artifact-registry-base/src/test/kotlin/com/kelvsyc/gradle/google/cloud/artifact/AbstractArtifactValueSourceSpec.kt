package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetFileRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.registerIfAbsent
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
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val slot = slot<GetFileRequest>()
            val mockFile = mockk<com.google.devtools.artifactregistry.v1.File>(relaxed = true)
            every { client.getFile(capture(slot)) } returns mockFile

            val provider = project.providers.ofKt(ConcreteArtifactValueSource::class) {
                parameters.service.set(service)
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
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val mockFile = mockk<com.google.devtools.artifactregistry.v1.File>(relaxed = true)
            every { client.getFile(any<GetFileRequest>()) } returns mockFile

            val provider = project.providers.ofKt(ConcreteArtifactValueSource::class) {
                parameters.service.set(service)
                parameters.projectName.set("my-project")
                parameters.location.set("us-central1")
                parameters.repository.set("my-repo")
                parameters.filename.set("file.txt")
            }

            provider.get() shouldBe "obtained"
        }
    }
}
