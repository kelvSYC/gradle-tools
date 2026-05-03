package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.ParameterType
import aws.sdk.kotlin.services.ssm.model.PutParameterRequest
import aws.sdk.kotlin.services.ssm.model.PutParameterResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ssm.MockSsmClientInfoInternal
import com.kelvsyc.gradle.plugins.SsmKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class PutParameterActionSpec : FunSpec() {
    init {
        test("execute - passes correct parameter details to SSM") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SsmKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSsmClientInfo::class, MockSsmClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSsmClientInfo>("mock") {}

            val client = extension.getClient<SsmClient, MockSsmClientInfo>("mock").get()!!
            val requestSlot = slot<PutParameterRequest>()
            coEvery { client.putParameter(capture(requestSlot)) } returns mockk<PutParameterResponse>()

            val params = project.objects.newInstance<PutParameterAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.parameterName.set("/my/parameter")
            params.parameterValue.set("new-value")
            params.parameterType.set("SecureString")
            params.overwrite.set(true)

            val action = object : PutParameterAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.name shouldBe "/my/parameter"
            captured.value shouldBe "new-value"
            captured.type shouldBe ParameterType.SecureString
            captured.overwrite shouldBe true
        }
    }
}
