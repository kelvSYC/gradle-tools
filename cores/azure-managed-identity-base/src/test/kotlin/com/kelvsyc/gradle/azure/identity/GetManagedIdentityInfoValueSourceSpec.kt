package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureManagedIdentityInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetManagedIdentityInfoValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns clientId and objectId map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val info = AzureManagedIdentityInfo(
                clientId = "client-abc",
                objectId = "object-xyz",
            )
            val call = mockk<Call<AzureManagedIdentityInfo>>()
            every { call.execute() } returns Response.success(info)
            every { imdsService.getManagedIdentityInfo(any()) } returns call

            val provider = project.providers.ofKt(GetManagedIdentityInfoValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2018-02-01")
            }
            val result = provider.get()

            result shouldContain ("clientId" to "client-abc")
            result shouldContain ("objectId" to "object-xyz")
        }
    }
}
