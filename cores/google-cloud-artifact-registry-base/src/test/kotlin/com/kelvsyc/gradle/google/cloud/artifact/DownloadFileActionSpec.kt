package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetFileRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files
import com.google.devtools.artifactregistry.v1.File as ArtifactRegistryFile

class DownloadFileActionSpec : FunSpec() {
    init {
        test("execute - resolves file resource name and writes response to outputFile") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val mockFile = mockk<ArtifactRegistryFile>(relaxed = true)
            val slot = slot<GetFileRequest>()
            every { client.getFile(capture(slot)) } returns mockFile

            val outputFile = Files.createTempFile("ar-download", ".bin")

            val params = project.objects.newInstance<DownloadFileAction.Parameters>()
            params.service.set(service)
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
