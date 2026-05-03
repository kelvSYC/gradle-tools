package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.lambda.MockLambdaClientInfoInternal
import com.kelvsyc.gradle.plugins.LambdaJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeResponse

class UpdateFunctionCodeActionSpec : FunSpec() {
    init {
        test("execute - uploads zip file bytes to Lambda") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(LambdaJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}

            val client = extension.getClient<LambdaClient, _>("mock").get()
            val requestSlot = slot<UpdateFunctionCodeRequest>()
            every { client.updateFunctionCode(capture(requestSlot)) } returns mockk<UpdateFunctionCodeResponse>()

            val zipPath = project.layout.buildDirectory.file("fn.zip").get().asFile
            zipPath.parentFile.mkdirs()
            val expectedBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
            zipPath.writeBytes(expectedBytes)

            val params = project.objects.newInstance<UpdateFunctionCodeAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.functionName.set("my-fn")
            params.zipFile.set(zipPath)
            params.publish.set(true)

            val action = object : UpdateFunctionCodeAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.functionName() shouldBe "my-fn"
            captured.publish() shouldBe true
            captured.zipFile().asByteArray().toList() shouldBe expectedBytes.toList()
        }
    }
}
