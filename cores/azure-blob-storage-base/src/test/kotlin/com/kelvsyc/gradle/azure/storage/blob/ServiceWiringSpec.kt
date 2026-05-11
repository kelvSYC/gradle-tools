package com.kelvsyc.gradle.azure.storage.blob

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.AzureBlobStorageBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ServiceWiringSpec : FunSpec() {
    init {
        context("BatchDownloadFromAzureBlobStorage - service wiring") {
            test("service is resolved from the ClientsBasePlugin service") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(AzureBlobStorageBasePlugin::class)

                val task = project.tasks.register(
                    "download",
                    BatchDownloadFromAzureBlobStorage::class.java
                ).get()

                task.service.shouldNotBeNull()
                task.service.get().shouldNotBeNull()
            }

            test("service resolves to the same service registered by ClientsBasePlugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(AzureBlobStorageBasePlugin::class)

                val task = project.tasks.register(
                    "download",
                    BatchDownloadFromAzureBlobStorage::class.java
                ).get()
                val expectedService = project.the<ClientsBaseExtension>().service.get()

                task.service.get() shouldBe expectedService
            }
        }

        context("BatchUploadToAzureBlobStorage - service wiring") {
            test("service is resolved from the ClientsBasePlugin service") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(AzureBlobStorageBasePlugin::class)

                val task = project.tasks.register(
                    "upload",
                    BatchUploadToAzureBlobStorage::class.java
                ).get()

                task.service.shouldNotBeNull()
                task.service.get().shouldNotBeNull()
            }

            test("service resolves to the same service registered by ClientsBasePlugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(AzureBlobStorageBasePlugin::class)

                val task = project.tasks.register(
                    "upload",
                    BatchUploadToAzureBlobStorage::class.java
                ).get()
                val expectedService = project.the<ClientsBaseExtension>().service.get()

                task.service.get() shouldBe expectedService
            }
        }
    }
}
