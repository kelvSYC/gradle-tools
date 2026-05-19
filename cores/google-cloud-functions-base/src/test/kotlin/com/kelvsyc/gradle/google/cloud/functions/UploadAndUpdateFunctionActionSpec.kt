package com.kelvsyc.gradle.google.cloud.functions

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.functions.v2.FunctionServiceClient
import com.google.cloud.functions.v2.GenerateUploadUrlRequest
import com.google.cloud.functions.v2.GenerateUploadUrlResponse
import com.google.cloud.functions.v2.GetFunctionRequest
import com.google.cloud.functions.v2.OperationMetadata
import com.google.cloud.functions.v2.StorageSource
import com.google.cloud.functions.v2.UpdateFunctionRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import com.google.cloud.functions.v2.Function as GcpFunction

class UploadAndUpdateFunctionActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - uploads zip to signed URL and updates function source") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions",
                MockFunctionServiceClientBuildService::class,
            )

            val storageSource = StorageSource.newBuilder()
                .setBucket("gcf-sources")
                .setObject("upload-abc123.zip")
                .build()
            val uploadResponse = GenerateUploadUrlResponse.newBuilder()
                .setUploadUrl("https://storage.googleapis.com/gcf-sources/upload-abc123.zip?X-Goog-Signature=sig")
                .setStorageSource(storageSource)
                .build()

            val uploadUrlSlot = slot<GenerateUploadUrlRequest>()
            every { client.generateUploadUrl(capture(uploadUrlSlot)) } returns uploadResponse

            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse

            val httpRequestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(httpRequestSlot)) } returns mockCall

            val existingFunction = GcpFunction.newBuilder()
                .setName("projects/p/locations/us-central1/functions/my-fn")
                .build()
            every { client.getFunction(any<GetFunctionRequest>()) } returns existingFunction

            val updateSlot = slot<UpdateFunctionRequest>()
            val operationFuture = mockk<OperationFuture<GcpFunction, OperationMetadata>>()
            every { operationFuture.get() } returns existingFunction
            every { client.updateFunctionAsync(capture(updateSlot)) } returns operationFuture

            val zipFile = File.createTempFile("function", ".zip")
            zipFile.deleteOnExit()
            zipFile.writeBytes("fake-zip-content".toByteArray())

            val params = project.objects.newInstance<UploadAndUpdateFunctionAction.Parameters>()
            params.service.set(service)
            params.functionName.set("projects/p/locations/us-central1/functions/my-fn")
            params.zipFile.set(zipFile)

            val action = object : UploadAndUpdateFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            uploadUrlSlot.captured.parent shouldBe "projects/p/locations/us-central1"

            httpRequestSlot.captured.url.toString() shouldBe
                "https://storage.googleapis.com/gcf-sources/upload-abc123.zip?X-Goog-Signature=sig"
            httpRequestSlot.captured.method shouldBe "PUT"
            httpRequestSlot.captured.body?.contentType()?.toString() shouldBe "application/zip"

            val updatedSource = updateSlot.captured.function.buildConfig.source.storageSource
            updatedSource.bucket shouldBe "gcf-sources"
            updatedSource.getObject() shouldBe "upload-abc123.zip"
            updateSlot.captured.updateMask.pathsList shouldBe listOf("build_config.source")
        }
    }
}
