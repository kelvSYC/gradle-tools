package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.EncryptResponse
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

class EncryptActionSpec : FunSpec() {
    init {
        test("execute - encrypts plaintext file and writes ciphertext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val plaintext = "hello world".toByteArray()
            val ciphertext = "encrypted".toByteArray()
            val plaintextFile = File.createTempFile("plain", ".bin").also { it.writeBytes(plaintext) }
            val ciphertextFile = File.createTempFile("cipher", ".bin")

            val slot = slot<EncryptRequest>()
            every { client.encrypt(capture(slot)) } returns
                EncryptResponse.newBuilder()
                    .setCiphertext(ByteString.copyFrom(ciphertext))
                    .build()

            val params = project.objects.newInstance<EncryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyName.set("projects/p/locations/global/keyRings/r/cryptoKeys/k")
            params.plaintextFile.set(plaintextFile)
            params.ciphertextFile.set(ciphertextFile)

            val action = object : EncryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            ciphertextFile.readBytes() shouldBe ciphertext
            slot.captured.name shouldBe "projects/p/locations/global/keyRings/r/cryptoKeys/k"
            slot.captured.plaintext shouldBe ByteString.copyFrom(plaintext)
        }
    }
}
