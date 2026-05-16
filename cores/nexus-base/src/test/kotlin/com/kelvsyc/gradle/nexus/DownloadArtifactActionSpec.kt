package com.kelvsyc.gradle.nexus

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.ResponseBody
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response
import java.io.File

class DownloadArtifactActionSpec : FunSpec() {
    init {
        test("streams response body to outputFile") {
            val project = ProjectBuilder.builder().build()
            val mockService = mockk<NexusService>()
            MockNexusClientBuildService.mockClient = mockService
            val service = project.gradle.sharedServices.registerIfAbsent(
                "nexus",
                MockNexusClientBuildService::class,
            )

            val expectedBytes = "artifact content".toByteArray()
            val responseBody = mockk<ResponseBody>()
            every { responseBody.byteStream() } returns expectedBytes.inputStream()
            val call = mockk<Call<ResponseBody>>()
            every { call.execute() } returns Response.success(responseBody)
            every { mockService.downloadAsset("my-repo", "path/to/artifact.jar") } returns call

            val tmpFile = File.createTempFile("test-artifact", ".jar")
            tmpFile.deleteOnExit()

            val params = project.objects.newInstance<DownloadArtifactAction.Parameters>()
            params.service.set(service)
            params.repository.set("my-repo")
            params.path.set("path/to/artifact.jar")
            params.outputFile.set(tmpFile)

            val action = object : DownloadArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            tmpFile.readBytes() shouldBe expectedBytes
        }
    }
}
