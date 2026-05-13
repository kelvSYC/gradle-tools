package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
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
            val client = mockk<Artifactory>()
            MockArtifactoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("artifactory", MockArtifactoryClientBuildService::class)

            val repoHandle = mockk<RepositoryHandle>()
            val downloadable = mockk<DownloadableArtifact>()

            every { client.repository("my-repo") } returns repoHandle
            every { repoHandle.download("my/path") } returns downloadable
            every { downloadable.doDownload() } returns ByteArrayInputStream("test-content".toByteArray())

            val params = project.objects.newInstance<AbstractArtifactValueSource.Parameters>()
            params.service.set(service)
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
