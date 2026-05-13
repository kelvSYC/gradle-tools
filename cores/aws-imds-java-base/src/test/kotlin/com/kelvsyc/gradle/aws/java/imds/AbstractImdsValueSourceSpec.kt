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

class AbstractImdsValueSourceSpec : FunSpec() {
    abstract class ListImdsValueSource : AbstractImdsValueSource<List<String>, AbstractImdsValueSource.Parameters>() {
        override fun doObtain(response: Ec2MetadataResponse): List<String> = response.asList()
    }

    init {
        test("obtain - calls specified path and returns doObtain result") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<Ec2MetadataClient>()
            MockImdsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("imds", MockImdsClientBuildService::class)

            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("sg-1\nsg-2\nsg-3")

            val provider = project.providers.ofKt(ListImdsValueSource::class) {
                parameters.service.set(service)
                parameters.path.set("/latest/meta-data/security-groups")
            }

            provider.get() shouldBe listOf("sg-1", "sg-2", "sg-3")
            pathSlot.captured shouldBe "/latest/meta-data/security-groups"
        }
    }
}
