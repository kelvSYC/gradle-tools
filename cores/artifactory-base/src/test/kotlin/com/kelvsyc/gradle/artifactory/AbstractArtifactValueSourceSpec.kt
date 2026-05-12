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
import java.io.InputStream

class AbstractArtifactValueSourceSpec : FunSpec() {
    abstract class TestValueSource : AbstractArtifactValueSource<String, AbstractArtifactValueSource.Parameters>() {
        override fun doObtain(input: InputStream): String = input.bufferedReader().readText()
    }

    init {
        test("obtain - downloads artifact and transforms via doObtain") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ArtifactoryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockArtifactoryClientInfo::class, MockArtifactoryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactoryClientInfo>("mock") {}

            val client = extension.getClient<Artifactory, MockArtifactoryClientInfo>("mock").get()
            val repoHandle = mockk<RepositoryHandle>()
            val downloadable = mockk<DownloadableArtifact>()

            every { client.repository("my-repo") } returns repoHandle
            every { repoHandle.download("my/path") } returns downloadable
            every { downloadable.doDownload() } returns ByteArrayInputStream("test-content".toByteArray())

            val params = project.objects.newInstance<AbstractArtifactValueSource.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.repository.set("my-repo")
            params.path.set("my/path")

            val valueSource = object : TestValueSource() {
                override fun getParameters() = params
            }
            val result = valueSource.obtain()

            result shouldBe "test-content"
            verify { repoHandle.download("my/path") }
        }
    }
}
