package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.DecryptRequest
import com.google.cloud.kms.v1.DecryptResponse
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

class DecryptActionSpec : FunSpec() {
    init {
        test("execute - decrypts ciphertext file and writes plaintext to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}

            val ciphertext = "encrypted".toByteArray()
            val plaintext = "hello world".toByteArray()
            val ciphertextFile = File.createTempFile("cipher", ".bin").also { it.writeBytes(ciphertext) }
            val plaintextFile = File.createTempFile("plain", ".bin")

            val slot = slot<DecryptRequest>()
            every { client.decrypt(capture(slot)) } returns
                DecryptResponse.newBuilder()
                    .setPlaintext(ByteString.copyFrom(plaintext))
                    .build()

            val params = project.objects.newInstance<DecryptAction.Parameters>()
            params.service.set(service)
            params.cryptoKeyName.set("projects/p/locations/global/keyRings/r/cryptoKeys/k")
            params.ciphertextFile.set(ciphertextFile)
            params.plaintextFile.set(plaintextFile)

            val action = object : DecryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            plaintextFile.readBytes() shouldBe plaintext
            slot.captured.name shouldBe "projects/p/locations/global/keyRings/r/cryptoKeys/k"
            slot.captured.ciphertext shouldBe ByteString.copyFrom(ciphertext)
        }
    }
}
