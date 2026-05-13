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
import org.jfrog.artifactory.client.ArtifactoryRequest
import org.jfrog.artifactory.client.ArtifactoryStreamingResponse
import java.io.ByteArrayInputStream

class AbstractStreamingRequestValueSourceSpec : FunSpec() {
    abstract class TestStreamingValueSource :
        AbstractStreamingRequestValueSource<String, AbstractStreamingRequestValueSource.Parameters>() {
        override fun doObtain(response: ArtifactoryStreamingResponse): String =
            response.inputStream.bufferedReader().readText()
    }

    init {
        test("obtain - performs streaming request and transforms via doObtain") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Artifactory>()
            MockArtifactoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("artifactory", MockArtifactoryClientBuildService::class)

            val request = mockk<ArtifactoryRequest>()
            val response = mockk<ArtifactoryStreamingResponse>()

            every { client.streamingRestCall(request) } returns response
            every { response.inputStream } returns ByteArrayInputStream("streaming-content".toByteArray())

            val params = project.objects.newInstance<AbstractStreamingRequestValueSource.Parameters>()
            params.service.set(service)
            params.request.set(request)

            val valueSource = object : TestStreamingValueSource() {
                override fun getParameters() = params
            }
            val result = valueSource.obtain()

            result shouldBe "streaming-content"
            verify { client.streamingRestCall(request) }
        }
    }
}
