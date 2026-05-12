package com.kelvsyc.gradle.aws.java.kms

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.EncryptRequest
import software.amazon.awssdk.services.kms.model.EncryptResponse

class EncryptActionSpec : FunSpec() {
    init {
        test("execute - sends plaintext bytes and writes returned ciphertext blob") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            val requestSlot = slot<EncryptRequest>()
            val expectedCiphertext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
            val response = mockk<EncryptResponse>()
            every { response.ciphertextBlob() } returns SdkBytes.fromByteArray(expectedCiphertext)
            every { client.encrypt(capture(requestSlot)) } returns response

            val plaintextPath = project.layout.buildDirectory.file("plain.txt").get().asFile
            plaintextPath.parentFile.mkdirs()
            val plaintextBytes = "hello".toByteArray()
            plaintextPath.writeBytes(plaintextBytes)
            val ciphertextPath = project.layout.buildDirectory.file("cipher.bin").get().asFile

            val params = project.objects.newInstance<EncryptAction.Parameters>()
            params.service.set(service)
            params.keyId.set("alias/my-key")
            params.plaintextFile.set(plaintextPath)
            params.ciphertextFile.set(ciphertextPath)

            val action = object : EncryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.keyId() shouldBe "alias/my-key"
            captured.plaintext().asByteArray().toList() shouldBe plaintextBytes.toList()
            ciphertextPath.readBytes().toList() shouldBe expectedCiphertext.toList()
        }
    }
}
