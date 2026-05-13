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

class AbstractImdsValueSourceSpec : FunSpec() {
    abstract class UpperCaseImdsValueSource : AbstractImdsValueSource<String, AbstractImdsValueSource.Parameters>() {
        override fun doObtain(response: String): String = response.uppercase()
    }

    init {
        test("obtain - calls specified path and returns doObtain result") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery { client.get("/latest/meta-data/instance-type") } returns "t3.micro"

            val provider = project.providers.ofKt(UpperCaseImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/instance-type")
            }

            provider.get() shouldBe "T3.MICRO"
        }

        test("obtain - returns null on EC2MetadataError") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery { client.get(any()) } throws EC2MetadataError(HttpStatusCode.NotFound, "not found")

            val provider = project.providers.ofKt(UpperCaseImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/instance-type")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
