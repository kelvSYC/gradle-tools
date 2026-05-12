package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.DecryptRequest
import aws.sdk.kotlin.services.kms.model.DecryptResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DecryptActionSpec : FunSpec() {
    init {
        test("execute - sends ciphertext bytes and writes returned plaintext") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KmsClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kms", MockKmsClientBuildService::class)
            val requestSlot = slot<DecryptRequest>()
            val expectedPlaintext = "hello".toByteArray()
            coEvery { client.decrypt(capture(requestSlot)) } returns DecryptResponse {
                plaintext = expectedPlaintext
            }

            val ciphertextPath = project.layout.buildDirectory.file("cipher.bin").get().asFile
            ciphertextPath.parentFile.mkdirs()
            val ciphertextBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
            ciphertextPath.writeBytes(ciphertextBytes)
            val plaintextPath = project.layout.buildDirectory.file("plain.txt").get().asFile

            val params = project.objects.newInstance<DecryptAction.Parameters>()
            params.service.set(service)
            params.ciphertextFile.set(ciphertextPath)
            params.plaintextFile.set(plaintextPath)

            val action = object : DecryptAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.ciphertextBlob?.toList() shouldBe ciphertextBytes.toList()
            plaintextPath.readBytes().toList() shouldBe expectedPlaintext.toList()
        }
    }
}
