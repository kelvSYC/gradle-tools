package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.GetCallerIdentityRequest
import aws.sdk.kotlin.services.sts.model.GetCallerIdentityResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.sts.MockStsClientInfoInternal
import com.kelvsyc.gradle.plugins.StsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.coEvery
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetCallerIdentityValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns identity fields keyed by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val client = extension.getClient<StsClient, MockStsClientInfo>("mock").get()!!
            coEvery { client.getCallerIdentity(any<GetCallerIdentityRequest>()) } returns GetCallerIdentityResponse {
                account = "123456789012"
                arn = "arn:aws:iam::123456789012:user/build-bot"
                userId = "AIDA1234EXAMPLE"
            }

            val provider = project.providers.of(GetCallerIdentityValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldHaveSize 3
            result shouldContain ("account" to "123456789012")
            result shouldContain ("arn" to "arn:aws:iam::123456789012:user/build-bot")
            result shouldContain ("userId" to "AIDA1234EXAMPLE")
        }
    }
}
