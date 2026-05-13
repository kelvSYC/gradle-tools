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

class AbstractInstanceIdentityValueSourceSpec : FunSpec() {
    abstract class StringInstanceIdentityValueSource :
        AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {
        override fun doObtain(document: String): String = document
    }

    init {
        test("obtain - returns result of doObtain when IMDS call succeeds") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery {
                client.get(AbstractInstanceIdentityValueSource.DOCUMENT_REQUEST_PATH)
            } returns "document-content"

            val provider = project.providers.ofKt(StringInstanceIdentityValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldBe "document-content"
        }

        test("obtain - returns null on EC2MetadataError") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ImdsClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)
            coEvery { client.get(any()) } throws EC2MetadataError(HttpStatusCode.NotFound, "not found")

            val provider = project.providers.ofKt(StringInstanceIdentityValueSource::class) {
                parameters.service.set(service)
            }

            provider.orNull.shouldBeNull()
        }
    }
}
