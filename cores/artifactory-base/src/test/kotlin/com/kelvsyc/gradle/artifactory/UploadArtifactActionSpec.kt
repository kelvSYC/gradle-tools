package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.RepositoryHandle
import org.jfrog.artifactory.client.UploadableArtifact
import java.io.File

class UploadArtifactActionSpec : FunSpec() {
    init {
        test("execute - uploads file to correct repository and path") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Artifactory>()
            MockArtifactoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("artifactory", MockArtifactoryClientBuildService::class)

            val repoHandle = mockk<RepositoryHandle>()
            val uploadable = mockk<UploadableArtifact>()
            val fileSlot = slot<File>()

            every { client.repository("my-repo") } returns repoHandle
            every { repoHandle.upload("my/path", capture(fileSlot)) } returns uploadable
            every { uploadable.doUpload() } returns mockk()

            val inputFile = tempfile()
            inputFile.writeText("upload-content")

            val params = project.objects.newInstance<UploadArtifactAction.Parameters>()
            params.service.set(service)
            params.repository.set("my-repo")
            params.path.set("my/path")
            params.inputFile.set(inputFile)

            val action = object : UploadArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { repoHandle.upload("my/path", any<File>()) }
            verify { uploadable.doUpload() }
        }
    }
}
