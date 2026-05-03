package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.imds.MockImdsClientInfoInternal
import com.kelvsyc.gradle.plugins.ImdsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
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
            project.pluginManager.apply(ImdsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<Ec2MetadataClient, MockImdsClientInfo>("mock").get()!!
            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("sg-1\nsg-2\nsg-3")

            val provider = project.providers.of(ListImdsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.path.set("/latest/meta-data/security-groups")
            }

            provider.get() shouldBe listOf("sg-1", "sg-2", "sg-3")
            pathSlot.captured shouldBe "/latest/meta-data/security-groups"
        }
    }
}
