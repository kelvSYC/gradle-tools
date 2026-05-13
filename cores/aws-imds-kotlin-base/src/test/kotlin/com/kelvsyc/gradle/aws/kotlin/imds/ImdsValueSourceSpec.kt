package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import aws.smithy.kotlin.runtime.http.HttpStatusCode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ImdsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns raw string from IMDS path") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery { client.get("/latest/meta-data/instance-id") } returns "i-0123456789abcdef0"

            val provider = project.providers.ofKt(ImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/instance-id")
            }

            provider.get() shouldBe "i-0123456789abcdef0"
        }

        test("obtain - returns null on EC2MetadataError") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery { client.get(any()) } throws EC2MetadataError(HttpStatusCode.NotFound, "not found")

            val provider = project.providers.ofKt(ImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/instance-id")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
