package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.artifactory.MockArtifactoryClientInfoInternal
import com.kelvsyc.gradle.plugins.ArtifactoryBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.DownloadableArtifact
import org.jfrog.artifactory.client.RepositoryHandle
import java.io.ByteArrayInputStream
import java.nio.file.Files

class DownloadArtifactActionSpec : FunSpec() {
    init {
        test("execute - downloads artifact to output file") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ArtifactoryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockArtifactoryClientInfo::class, MockArtifactoryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactoryClientInfo>("mock") {}

            val client = extension.getClient<Artifactory, MockArtifactoryClientInfo>("mock").get()
            val repoHandle = mockk<RepositoryHandle>()
            val downloadable = mockk<DownloadableArtifact>()
            val content = "artifact-content"

            every { client.repository("my-repo") } returns repoHandle
            every { repoHandle.download("my/path") } returns downloadable
            every { downloadable.doDownload() } returns ByteArrayInputStream(content.toByteArray())

            val outputFile = Files.createTempFile("download-test", ".bin")

            val params = project.objects.newInstance<DownloadArtifactAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.repository.set("my-repo")
            params.path.set("my/path")
            params.outputFile.set(outputFile.toFile())

            val action = object : DownloadArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.toFile().readText() shouldBe content
            verify { repoHandle.download("my/path") }

            outputFile.toFile().delete()
        }
    }
}
