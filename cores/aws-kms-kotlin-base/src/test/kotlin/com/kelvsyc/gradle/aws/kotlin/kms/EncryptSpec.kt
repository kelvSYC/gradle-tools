package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.EncryptRequest
import aws.sdk.kotlin.services.kms.model.EncryptResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class EncryptSpec : FunSpec({
    test("execute - sends plaintext bytes and writes returned ciphertext blob") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<KmsClient>()
        MockKmsClientBuildService.mockClient = client
        val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
        val requestSlot = slot<EncryptRequest>()
        val expectedCiphertext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        coEvery { client.encrypt(capture(requestSlot)) } returns EncryptResponse {
            ciphertextBlob = expectedCiphertext
        }

        val plaintextPath = project.layout.buildDirectory.file("plain.txt").get().asFile
        plaintextPath.parentFile.mkdirs()
        val plaintextBytes = "hello".toByteArray()
        plaintextPath.writeBytes(plaintextBytes)
        val ciphertextPath = project.layout.buildDirectory.file("cipher.bin").get().asFile

        val task = project.tasks.create("encrypt", Encrypt::class.java)
        task.service.set(service)
        task.keyId.set("alias/my-key")
        task.plaintextFile.set(plaintextPath)
        task.ciphertextFile.set(ciphertextPath)

        task.execute()

        val captured = requestSlot.captured
        captured.keyId shouldBe "alias/my-key"
        captured.plaintext?.toList() shouldBe plaintextBytes.toList()
        ciphertextPath.readBytes().toList() shouldBe expectedCiphertext.toList()
        MockKmsClientBuildService.mockClient = null
    }
})
