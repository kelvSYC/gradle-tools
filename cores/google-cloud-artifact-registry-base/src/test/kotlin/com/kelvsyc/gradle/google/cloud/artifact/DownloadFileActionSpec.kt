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
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files
import com.google.devtools.artifactregistry.v1.File as ArtifactRegistryFile

class DownloadFileActionSpec : FunSpec() {
    init {
        test("execute - resolves file resource name and writes response to outputFile") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val mockFile = mockk<ArtifactRegistryFile>(relaxed = true)
            val slot = slot<GetFileRequest>()
            every { client.getFile(capture(slot)) } returns mockFile

            val outputFile = Files.createTempFile("ar-download", ".bin")

            val params = project.objects.newInstance<DownloadFileAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.projectName.set("my-project")
            params.location.set("us-east1")
            params.repository.set("my-repo")
            params.filename.set("file.txt")
            params.outputFile.set(outputFile.toFile())

            val action = object : DownloadFileAction() {
                override fun getParameters() = params
            }
            action.execute()

            slot.captured.name.startsWith("projects/my-project/locations/us-east1/repositories/my-repo/files/") shouldBe true

            outputFile.toFile().delete()
        }
    }
}
