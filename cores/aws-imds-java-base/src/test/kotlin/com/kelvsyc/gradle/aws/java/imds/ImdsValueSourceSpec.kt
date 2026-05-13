package com.kelvsyc.gradle.aws.java.imds

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.imds.Ec2MetadataResponse

class ImdsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns raw string from IMDS path") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Ec2MetadataClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)

            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("i-0123456789abcdef0")

            val provider = project.providers.ofKt(ImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/instance-id")
            }

            provider.get() shouldBe "i-0123456789abcdef0"
            pathSlot.captured shouldBe "/latest/meta-data/instance-id"
        }
    }
}
