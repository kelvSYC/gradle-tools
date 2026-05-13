package com.kelvsyc.gradle.aws.java.imds

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.document.Document
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.imds.Ec2MetadataResponse

class AbstractInstanceIdentityValueSourceSpec : FunSpec() {
    abstract class StringInstanceIdentityValueSource :
        AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {
        override fun doObtain(document: Document): String = document.asString()
    }

    abstract class NullReturningInstanceIdentityValueSource :
        AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {
        override fun doObtain(document: Document): String? = null
    }

    init {
        test("obtain - calls correct path and returns doObtain result") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Ec2MetadataClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)

            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("\"hello\"")

            val provider = project.providers.ofKt(StringInstanceIdentityValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldBe "hello"
            pathSlot.captured shouldBe AbstractInstanceIdentityValueSource.DOCUMENT_REQUEST_PATH
        }

        test("obtain - returns null when doObtain returns null") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Ec2MetadataClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)

            every { client.get(any()) } returns Ec2MetadataResponse.create("\"hello\"")

            val provider = project.providers.ofKt(NullReturningInstanceIdentityValueSource::class) {
                parameters.service.set(service)
            }

            provider.orNull.shouldBeNull()
        }
    }
}
