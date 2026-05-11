package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.azure.storage.blob.BlobContainerAsyncClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobContainerClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceAsyncClientInfo
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientInfo
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import kotlin.jvm.java

class AzureBlobStorageBasePluginSpec : FunSpec() {
    init {
        test("Apply - Registers all client info types") {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply(AzureBlobStorageBasePlugin::class)

            val service = project.the<ClientsBaseExtension>().service.get()
            val clients = service.registrations as PolymorphicDomainObjectContainerInternal<*>
            clients.createableTypes shouldContain BlobServiceClientInfo::class.java
            clients.createableTypes shouldContain BlobServiceAsyncClientInfo::class.java
            clients.createableTypes shouldContain BlobContainerClientInfo::class.java
            clients.createableTypes shouldContain BlobContainerAsyncClientInfo::class.java
        }
    }
}
