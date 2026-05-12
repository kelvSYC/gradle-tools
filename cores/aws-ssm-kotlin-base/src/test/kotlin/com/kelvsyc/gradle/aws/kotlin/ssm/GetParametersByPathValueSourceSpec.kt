package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParametersByPathRequest
import aws.sdk.kotlin.services.ssm.model.GetParametersByPathResponse
import aws.sdk.kotlin.services.ssm.model.Parameter
import aws.sdk.kotlin.services.ssm.paginators.getParametersByPathPaginated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetParametersByPathValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of parameter names to values") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            val requestSlot = slot<GetParametersByPathRequest>()

            mockkStatic("aws.sdk.kotlin.services.ssm.paginators.PaginatorsKt")
            every { client.getParametersByPathPaginated(capture(requestSlot)) } returns flowOf(
                GetParametersByPathResponse {
                    parameters = listOf(
                        Parameter { name = "/app/one"; value = "value-one" },
                        Parameter { name = "/app/two"; value = "value-two" },
                    )
                }
            )

            val provider = project.providers.ofKt(GetParametersByPathValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/app/")
                parameters.recursive.set(true)
                parameters.withDecryption.set(true)
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("/app/one" to "value-one")
            result shouldContain ("/app/two" to "value-two")
            requestSlot.captured.path shouldBe "/app/"
            requestSlot.captured.recursive shouldBe true
            requestSlot.captured.withDecryption shouldBe true
        }
    }
}
