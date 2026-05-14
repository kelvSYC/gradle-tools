package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricSignRequest
import com.google.cloud.kms.v1.AsymmetricSignResponse
import com.google.cloud.kms.v1.Digest
import com.google.cloud.kms.v1.KeyManagementServiceClient
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
import java.security.MessageDigest

class AsymmetricSignActionSpec : FunSpec() {
    init {
        test("execute - signs SHA-256 digest of data file and writes signature to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val data = "artifact content".toByteArray()
            val signature = "sig-bytes".toByteArray()
            val dataFile = File.createTempFile("data", ".bin").also { it.writeBytes(data) }
            val signatureFile = File.createTempFile("sig", ".bin")

            val slot = slot<AsymmetricSignRequest>()
            every { client.asymmetricSign(capture(slot)) } returns
                AsymmetricSignResponse.newBuilder()
                    .setSignature(ByteString.copyFrom(signature))
                    .build()

            val params = project.objects.newInstance<AsymmetricSignAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.digestAlgorithm.set("SHA256")
            params.dataFile.set(dataFile)
            params.signatureFile.set(signatureFile)

            val action = object : AsymmetricSignAction() {
                override fun getParameters() = params
            }
            action.execute()

            signatureFile.readBytes() shouldBe signature
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            val expectedDigest = MessageDigest.getInstance("SHA-256").digest(data)
            slot.captured.digest.sha256 shouldBe ByteString.copyFrom(expectedDigest)
        }
    }
}
