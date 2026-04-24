package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import aws.smithy.kotlin.runtime.http.HttpStatusCode
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.imds.MockImdsClientInfoInternal
import com.kelvsyc.gradle.plugins.ImdsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractInstanceIdentityValueSourceSpec : FunSpec() {
    abstract class StringInstanceIdentityValueSource :
        AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {
        override fun doObtain(document: String): String = document
    }

    init {
        test("obtain - returns result of doObtain when IMDS call succeeds") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ImdsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<aws.sdk.kotlin.runtime.config.imds.ImdsClient, MockImdsClientInfo>("mock").get()!!
            coEvery { client.get(AbstractInstanceIdentityValueSource.DOCUMENT_REQUEST_PATH) } returns "document-content"

            val provider = project.providers.of(StringInstanceIdentityValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }

            provider.get() shouldBe "document-content"
        }

        test("obtain - returns null on EC2MetadataError") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(ImdsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockImdsClientInfo::class, MockImdsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockImdsClientInfo>("mock") {}

            val client = extension.getClient<aws.sdk.kotlin.runtime.config.imds.ImdsClient, MockImdsClientInfo>("mock").get()!!
            coEvery { client.get(any()) } throws EC2MetadataError(HttpStatusCode.NotFound, "not found")

            val provider = project.providers.of(StringInstanceIdentityValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
