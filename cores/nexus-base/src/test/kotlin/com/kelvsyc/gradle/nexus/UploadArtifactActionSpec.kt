package com.kelvsyc.gradle.nexus

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response
import java.io.File

class UploadArtifactActionSpec : FunSpec() {
    init {
        test("calls uploadRawAsset with correct repository, directory, and filename") {
            val project = ProjectBuilder.builder().build()
            val mockService = mockk<NexusService>()
            MockNexusClientBuildService.mockClient = mockService
            val service = project.gradle.sharedServices.registerIfAbsent(
                "nexus",
                MockNexusClientBuildService::class,
            )

            val repositorySlot = slot<String>()
            val directorySlot = slot<RequestBody>()
            val filenameSlot = slot<RequestBody>()
            val partSlot = slot<MultipartBody.Part>()
            val mockCall = mockk<Call<ResponseBody>>()
            every { mockCall.execute() } returns Response.success(mockk<ResponseBody>())
            every {
                mockService.uploadRawAsset(
                    capture(repositorySlot),
                    capture(directorySlot),
                    capture(filenameSlot),
                    capture(partSlot),
                )
            } returns mockCall

            val tmpFile = File.createTempFile("artifact", ".jar")
            tmpFile.deleteOnExit()
            tmpFile.writeBytes("content".toByteArray())

            val params = project.objects.newInstance<UploadArtifactAction.Parameters>()
            params.service.set(service)
            params.repository.set("my-repo")
            params.path.set("com/example/1.0/artifact-1.0.jar")
            params.inputFile.set(tmpFile)

            val action = object : UploadArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            repositorySlot.captured shouldBe "my-repo"
            directorySlot.captured.readText() shouldBe "com/example/1.0"
            filenameSlot.captured.readText() shouldBe "artifact-1.0.jar"
        }

        test("path with no slash sends empty directory") {
            val project = ProjectBuilder.builder().build()
            val mockService = mockk<NexusService>()
            MockNexusClientBuildService.mockClient = mockService
            val service = project.gradle.sharedServices.registerIfAbsent(
                "nexus2",
                MockNexusClientBuildService::class,
            )

            val directorySlot = slot<RequestBody>()
            val filenameSlot = slot<RequestBody>()
            val mockCall = mockk<Call<ResponseBody>>()
            every { mockCall.execute() } returns Response.success(mockk<ResponseBody>())
            every {
                mockService.uploadRawAsset(
                    any(),
                    capture(directorySlot),
                    capture(filenameSlot),
                    any(),
                )
            } returns mockCall

            val tmpFile = File.createTempFile("artifact", ".jar")
            tmpFile.deleteOnExit()
            tmpFile.writeBytes("content".toByteArray())

            val params = project.objects.newInstance<UploadArtifactAction.Parameters>()
            params.service.set(service)
            params.repository.set("my-repo")
            params.path.set("artifact.jar")
            params.inputFile.set(tmpFile)

            val action = object : UploadArtifactAction() {
                override fun getParameters() = params
            }
            action.execute()

            directorySlot.captured.readText() shouldBe ""
            filenameSlot.captured.readText() shouldBe "artifact.jar"
        }
    }

    private fun RequestBody.readText(): String {
        val buffer = Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
    }
}
