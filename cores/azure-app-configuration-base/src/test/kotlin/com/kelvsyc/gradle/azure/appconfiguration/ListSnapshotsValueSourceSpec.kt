package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSnapshot
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListSnapshotsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns names of READY snapshots") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val readySnapshot1 = mockk<ConfigurationSnapshot>()
            every { readySnapshot1.name } returns "snapshot-1"
            every { readySnapshot1.status } returns ConfigurationSnapshotStatus.READY

            val readySnapshot2 = mockk<ConfigurationSnapshot>()
            every { readySnapshot2.name } returns "snapshot-2"
            every { readySnapshot2.status } returns ConfigurationSnapshotStatus.READY

            val archivedSnapshot = mockk<ConfigurationSnapshot>()
            every { archivedSnapshot.name } returns "snapshot-archived"
            every { archivedSnapshot.status } returns ConfigurationSnapshotStatus.ARCHIVED

            every { client.listSnapshots(any()) } returns
                mockk {
                    every { iterator() } returns
                        listOf(readySnapshot1, readySnapshot2, archivedSnapshot).toMutableList().iterator()
                }

            val provider = project.providers.ofKt(ListSnapshotsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldContainExactly listOf("snapshot-1", "snapshot-2")
        }

        test("obtain - returns empty list when no snapshots") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            every { client.listSnapshots(any()) } returns
                mockk { every { iterator() } returns listOf<ConfigurationSnapshot>().toMutableList().iterator() }

            val provider = project.providers.ofKt(ListSnapshotsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result.shouldBeEmpty()
        }

        test("obtain - applies name filter when set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val snapshot = mockk<ConfigurationSnapshot>()
            every { snapshot.name } returns "prod-snapshot"
            every { snapshot.status } returns ConfigurationSnapshotStatus.READY

            every { client.listSnapshots(any()) } returns
                mockk { every { iterator() } returns listOf(snapshot).toMutableList().iterator() }

            val provider = project.providers.ofKt(ListSnapshotsValueSource::class) {
                parameters.service.set(service)
                parameters.nameFilter.set("prod*")
            }
            val result = provider.get()

            result shouldContainExactly listOf("prod-snapshot")
        }
    }
}


