package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class AzureComputeMetadataValueSourceSpec : FunSpec() {
    init {
        test("obtain - maps non-null fields to string map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds", MockAzureImdsClientBuildService::class)

            val metadata = AzureComputeMetadata(
                subscriptionId = "sub-123",
                resourceGroupName = "rg-test",
                name = "my-vm",
                location = "eastus",
                vmId = "vm-id-456",
                vmSize = "Standard_D2s_v3",
                osType = "Linux",
            )
            val call = mockk<Call<AzureComputeMetadata>>()
            every { call.execute() } returns Response.success(metadata)
            every { imdsService.getComputeMetadata(any()) } returns call

            val provider = project.providers.ofKt(AzureComputeMetadataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldContain ("subscriptionId" to "sub-123")
            result shouldContain ("resourceGroupName" to "rg-test")
            result shouldContain ("name" to "my-vm")
            result shouldContain ("location" to "eastus")
            result shouldContain ("vmId" to "vm-id-456")
            result shouldContain ("vmSize" to "Standard_D2s_v3")
            result shouldContain ("osType" to "Linux")
        }

        test("obtain - omits null fields from map") {
            val project = ProjectBuilder.builder().build()
            val imdsService = mockk<AzureImdsService>()
            MockAzureImdsClientBuildService.mockClient = imdsService
            val service = project.gradle.sharedServices
                .registerIfAbsent("imds-partial", MockAzureImdsClientBuildService::class)

            val metadata = AzureComputeMetadata(
                subscriptionId = "sub-123",
                name = "my-vm",
            )
            val call = mockk<Call<AzureComputeMetadata>>()
            every { call.execute() } returns Response.success(metadata)
            every { imdsService.getComputeMetadata(any()) } returns call

            val provider = project.providers.ofKt(AzureComputeMetadataValueSource::class) {
                parameters.service.set(service)
                parameters.apiVersion.set("2021-02-01")
            }
            val result = provider.get()

            result shouldContain ("subscriptionId" to "sub-123")
            result shouldNotContainKey "location"
        }
    }
}
