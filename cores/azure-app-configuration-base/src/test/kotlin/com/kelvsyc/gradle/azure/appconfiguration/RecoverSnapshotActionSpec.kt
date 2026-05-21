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

class RecoverSnapshotActionSpec : FunSpec() {
    init {
        test("execute - recovers snapshot by name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val snapshotNameSlot = slot<String>()
            every { client.recoverSnapshot(capture(snapshotNameSlot)) } returns mockk<ConfigurationSnapshot>()

            val params = project.objects.newInstance<RecoverSnapshotAction.Parameters>()
            params.service.set(service)
            params.snapshotName.set("snapshot-recover")

            val action = object : RecoverSnapshotAction() {
                override fun getParameters() = params
            }
            action.execute()

            snapshotNameSlot.captured shouldBe "snapshot-recover"
        }
    }
}
