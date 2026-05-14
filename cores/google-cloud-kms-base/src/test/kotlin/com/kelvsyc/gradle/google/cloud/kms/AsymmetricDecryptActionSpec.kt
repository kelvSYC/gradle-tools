package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricDecryptRequest
import com.google.cloud.kms.v1.AsymmetricDecryptResponse
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

class AsymmetricDecryptActionSpec : FunSpec() {
    init {
        test("execute - decrypts ciphertext file and writes plaintext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val ciphertext = "rsa-encrypted".toByteArray()
            val plaintext = "secret data".toByteArray()
            val ciphertextFile = File.createTempFile("cipher", ".bin").also { it.writeBytes(ciphertext) }
            val plaintextFile = File.createTempFile("plain", ".bin")

            val slot = slot<AsymmetricDecryptRequest>()
            every { client.asymmetricDecrypt(capture(slot)) } returns
                AsymmetricDecryptResponse.newBuilder()
                    .setPlaintext(ByteString.copyFrom(plaintext))
                    .build()

            val params = project.objects.newInstance<AsymmetricDecryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyVersionName.set(
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            )
            params.ciphertextFile.set(ciphertextFile)
            params.plaintextFile.set(plaintextFile)

            val action = object : AsymmetricDecryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            plaintextFile.readBytes() shouldBe plaintext
            slot.captured.name shouldBe
                "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
            slot.captured.ciphertext shouldBe ByteString.copyFrom(ciphertext)
        }
    }
}
