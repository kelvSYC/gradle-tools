package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ArchiveSnapshotActionSpec : FunSpec() {
    init {
        test("execute - archives snapshot by name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val snapshotNameSlot = slot<String>()
            every { client.archiveSnapshot(capture(snapshotNameSlot)) } returns mockk<ConfigurationSnapshot>()

            val params = project.objects.newInstance<ArchiveSnapshotAction.Parameters>()
            params.service.set(service)
            params.snapshotName.set("snapshot-archive")

            val action = object : ArchiveSnapshotAction() {
                override fun getParameters() = params
            }
            action.execute()

            snapshotNameSlot.captured shouldBe "snapshot-archive"
        }
    }
}
