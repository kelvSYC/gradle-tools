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
            project.pluginManager.apply(ArtifactoryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockArtifactoryClientInfo::class, MockArtifactoryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactoryClientInfo>("mock") {}

            val client = extension.getClient<Artifactory, MockArtifactoryClientInfo>("mock").get()
            val request = mockk<ArtifactoryRequest>()
            val response = mockk<ArtifactoryStreamingResponse>()

            every { client.streamingRestCall(request) } returns response
            every { response.inputStream } returns ByteArrayInputStream("streaming-content".toByteArray())

            val params = project.objects.newInstance<AbstractStreamingRequestValueSource.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
