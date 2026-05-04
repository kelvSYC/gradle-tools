package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ecr.MockEcrClientInfoInternal
import com.kelvsyc.gradle.plugins.EcrJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.AuthorizationData
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse

class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns first authorization token") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(EcrJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockEcrClientInfo::class, MockEcrClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockEcrClientInfo>("mock") {}
            val client = extension.getClient<EcrClient, _>("mock").get()

            val authData = mockk<AuthorizationData>()
            every { authData.authorizationToken() } returns "QVdTOnRva2VuLXZhbHVl"

            val response = mockk<GetAuthorizationTokenResponse>()
            every { response.authorizationData() } returns listOf(authData)

            every { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) } returns response

            val provider = project.providers.of(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldBe "QVdTOnRva2VuLXZhbHVl"
        }
    }
}
