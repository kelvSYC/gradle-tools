package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.sts.MockStsClientInfoInternal
import com.kelvsyc.gradle.plugins.StsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse

class GetCallerIdentityValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns identity fields keyed by name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val client = extension.getClient<StsClient, _>("mock").get()
            val response = mockk<GetCallerIdentityResponse>()
            every { response.account() } returns "123456789012"
            every { response.arn() } returns "arn:aws:iam::123456789012:user/build-bot"
            every { response.userId() } returns "AIDA1234EXAMPLE"
            every { client.getCallerIdentity(any<GetCallerIdentityRequest>()) } returns response

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
