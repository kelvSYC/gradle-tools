package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureAttestedData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class AzureAttestedDataValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns signature string") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val data = AzureAttestedData(encoding = "pkcs7", signature = "base64sig==")
            val call = mockk<Call<AzureAttestedData>>()
            every { call.execute() } returns Response.success(data)
            every { imdsService.getAttestedData(any(), any()) } returns call

            val provider = project.providers.ofKt(AzureAttestedDataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldBe "base64sig=="
        }

        test("obtain - passes nonce when set") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds-nonce", MockAzureImdsClientBuildService::class)

            val data = AzureAttestedData(encoding = "pkcs7", signature = "sig==")
            val call = mockk<Call<AzureAttestedData>>()
            every { call.execute() } returns Response.success(data)
            every { imdsService.getAttestedData("2021-02-01", "abc123") } returns call

            val provider = project.providers.ofKt(AzureAttestedDataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
                parameters.nonce.set("abc123")
            }
            val result = provider.get()

            result shouldBe "sig=="
        }
    }
}
