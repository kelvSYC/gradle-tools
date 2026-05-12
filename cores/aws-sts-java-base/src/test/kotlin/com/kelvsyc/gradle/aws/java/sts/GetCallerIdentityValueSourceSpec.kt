package com.kelvsyc.gradle.aws.java.sts

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse

class GetCallerIdentityValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns identity fields keyed by name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            val response = mockk<GetCallerIdentityResponse>()
            every { response.account() } returns "123456789012"
            every { response.arn() } returns "arn:aws:iam::123456789012:user/build-bot"
            every { response.userId() } returns "AIDA1234EXAMPLE"
            every { client.getCallerIdentity(any<GetCallerIdentityRequest>()) } returns response

            val provider = project.providers.ofKt(GetCallerIdentityValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldHaveSize 3
            result shouldContain ("account" to "123456789012")
            result shouldContain ("arn" to "arn:aws:iam::123456789012:user/build-bot")
            result shouldContain ("userId" to "AIDA1234EXAMPLE")
        }
    }
}
