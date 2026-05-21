package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.util.polling.PollOperationDetails
import com.azure.core.util.Context
import com.azure.core.util.polling.SyncPoller
import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSnapshot
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateSnapshotActionSpec : FunSpec() {
    init {
        test("execute - creates snapshot with key filter") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val poller = mockk<SyncPoller<PollOperationDetails, ConfigurationSnapshot>>(relaxed = true)
            every {
                client.beginCreateSnapshot(any(), any(), any())
            } returns poller
            every { poller.waitForCompletion() } returns mockk()

            val params = project.objects.newInstance<CreateSnapshotAction.Parameters>()
            params.service.set(service)
            params.snapshotName.set("snapshot-1")
            params.keyFilter.set("app.*")

            val action = object : CreateSnapshotAction() {
                override fun getParameters() = params
            }
            action.execute()
        }

        test("execute - sets label filter when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val poller = mockk<SyncPoller<PollOperationDetails, ConfigurationSnapshot>>(relaxed = true)
            every {
                client.beginCreateSnapshot(any(), any(), any())
            } returns poller
            every { poller.waitForCompletion() } returns mockk()

            val params = project.objects.newInstance<CreateSnapshotAction.Parameters>()
            params.service.set(service)
            params.snapshotName.set("snapshot-prod")
            params.keyFilter.set("config.*")
            params.labelFilter.set("production")

            val action = object : CreateSnapshotAction() {
                override fun getParameters() = params
            }
            action.execute()
        }

        test("execute - sets retention period when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val poller = mockk<SyncPoller<PollOperationDetails, ConfigurationSnapshot>>(relaxed = true)
            every {
                client.beginCreateSnapshot(any(), any(), any())
            } returns poller
            every { poller.waitForCompletion() } returns mockk()

            val params = project.objects.newInstance<CreateSnapshotAction.Parameters>()
            params.service.set(service)
            params.snapshotName.set("snapshot-temp")
            params.keyFilter.set("temp.*")
            params.retentionPeriod.set(3600)

            val action = object : CreateSnapshotAction() {
                override fun getParameters() = params
            }
            action.execute()
        }
    }
}
