package com.kelvsyc.gradle.google.cloud.storage

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.plugins.GoogleCloudStorageBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ServiceWiringSpec : FunSpec() {
    init {
        context("BatchDownloadFromGCS - service wiring") {
            test("clientsService is resolved from the ClientsBasePlugin service") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GoogleCloudStorageBasePlugin::class)

                val task = project.tasks.register("download", BatchDownloadFromGCS::class.java).get()

                task.clientsService.shouldNotBeNull()
                task.clientsService.get().shouldNotBeNull()
            }

            test("clientsService resolves to the same service registered by ClientsBasePlugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GoogleCloudStorageBasePlugin::class)

                val task = project.tasks.register("download", BatchDownloadFromGCS::class.java).get()
                val expectedService = project.the<ClientsBaseExtension>().service.get()

                task.clientsService.get() shouldBe expectedService
            }
        }

        context("BatchUploadToGCS - service wiring") {
            test("service is resolved from the ClientsBasePlugin service") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GoogleCloudStorageBasePlugin::class)

                val task = project.tasks.register("upload", BatchUploadToGCS::class.java).get()

                task.service.shouldNotBeNull()
                task.service.get().shouldNotBeNull()
            }

            test("service resolves to the same service registered by ClientsBasePlugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GoogleCloudStorageBasePlugin::class)

                val task = project.tasks.register("upload", BatchUploadToGCS::class.java).get()
                val expectedService = project.the<ClientsBaseExtension>().service.get()

                task.service.get() shouldBe expectedService
            }
        }
    }
}
