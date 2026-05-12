package com.kelvsyc.gradle.aws.java.ssm

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.ParameterType
import software.amazon.awssdk.services.ssm.model.PutParameterRequest
import software.amazon.awssdk.services.ssm.model.PutParameterResponse

class PutParameterActionSpec : FunSpec() {
    init {
        test("execute - passes correct parameter details to SSM") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            val requestSlot = slot<PutParameterRequest>()
            every { client.putParameter(capture(requestSlot)) } returns mockk<PutParameterResponse>()

            val params = project.objects.newInstance<PutParameterAction.Parameters>()
            params.service.set(service)
            params.parameterName.set("/my/parameter")
            params.parameterValue.set("new-value")
            params.parameterType.set("SecureString")
            params.overwrite.set(true)

            val action = object : PutParameterAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.name() shouldBe "/my/parameter"
            captured.value() shouldBe "new-value"
            captured.type() shouldBe ParameterType.SECURE_STRING
            captured.overwrite() shouldBe true
        }
    }
}
