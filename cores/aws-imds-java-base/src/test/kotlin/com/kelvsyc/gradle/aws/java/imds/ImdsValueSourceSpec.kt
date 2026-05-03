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

class ImdsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns raw string from IMDS path") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ImdsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<Ec2MetadataClient, MockImdsClientInfo>("mock").get()!!
            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("i-0123456789abcdef0")

            val provider = project.providers.of(ImdsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.path.set("/latest/meta-data/instance-id")
            }

            provider.get() shouldBe "i-0123456789abcdef0"
            pathSlot.captured shouldBe "/latest/meta-data/instance-id"
        }
    }
}
