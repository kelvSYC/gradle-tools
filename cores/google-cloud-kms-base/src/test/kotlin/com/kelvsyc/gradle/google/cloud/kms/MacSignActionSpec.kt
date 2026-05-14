package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.MacSignRequest
import com.google.cloud.kms.v1.MacSignResponse
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class MacSignActionSpec : FunSpec() {
    init {
        test("execute - computes MAC over data file and writes mac to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val data = "payload data".toByteArray()
            val mac = "hmac-bytes".toByteArray()
            val dataFile = File.createTempFile("data", ".bin").also { it.writeBytes(data) }
            val macFile = File.createTempFile("mac", ".bin")

            val slot = slot<MacSignRequest>()
            every { client.macSign(capture(slot)) } returns
                MacSignResponse.newBuilder()
                    .setMac(ByteString.copyFrom(mac))
                    .build()

            val params = project.objects.newInstance<MacSignAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.dataFile.set(dataFile)
            params.macFile.set(macFile)

            val action = object : MacSignAction() {
                override fun getParameters() = params
            }
            action.execute()

            macFile.readBytes() shouldBe mac
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            slot.captured.data shouldBe ByteString.copyFrom(data)
        }
    }
}
