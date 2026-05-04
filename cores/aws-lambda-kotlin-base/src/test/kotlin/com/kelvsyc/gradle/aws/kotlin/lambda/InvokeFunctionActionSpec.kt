package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import aws.sdk.kotlin.services.lambda.model.InvokeResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.lambda.MockLambdaClientInfoInternal
import com.kelvsyc.gradle.plugins.LambdaKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class InvokeFunctionActionSpec : FunSpec() {
    init {
        test("execute - passes correct invocation parameters") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(LambdaKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}

            val client = extension.getClient<LambdaClient, MockLambdaClientInfo>("mock").get()!!
            val requestSlot = slot<InvokeRequest>()
            coEvery { client.invoke(capture(requestSlot)) } returns mockk<InvokeResponse>()

            val params = project.objects.newInstance<InvokeFunctionAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.functionName.set("my-fn")
            params.qualifier.set("prod")
            params.payload.set("{\"hello\":\"world\"}")
            params.invocationType.set("Event")

            val action = object : InvokeFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.functionName shouldBe "my-fn"
            captured.qualifier shouldBe "prod"
            captured.payload?.toString(Charsets.UTF_8) shouldBe "{\"hello\":\"world\"}"
            captured.invocationType shouldBe InvocationType.Event
        }
    }
}
