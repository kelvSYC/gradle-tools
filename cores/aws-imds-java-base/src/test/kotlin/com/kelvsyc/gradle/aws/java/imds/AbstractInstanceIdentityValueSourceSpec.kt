package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.imds.MockImdsClientInfoInternal
import com.kelvsyc.gradle.plugins.ImdsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
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
            project.pluginManager.apply(ImdsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<Ec2MetadataClient, MockImdsClientInfo>("mock").get()!!
            val pathSlot = slot<String>()
            every { client.get(capture(pathSlot)) } returns Ec2MetadataResponse.create("\"hello\"")

            val provider = project.providers.of(StringInstanceIdentityValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }

            provider.get() shouldBe "hello"
            pathSlot.captured shouldBe AbstractInstanceIdentityValueSource.DOCUMENT_REQUEST_PATH
        }

        test("obtain - returns null when doObtain returns null") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ImdsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<Ec2MetadataClient, MockImdsClientInfo>("mock").get()!!
            every { client.get(any()) } returns Ec2MetadataResponse.create("\"hello\"")

            val provider = project.providers.of(NullReturningInstanceIdentityValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
