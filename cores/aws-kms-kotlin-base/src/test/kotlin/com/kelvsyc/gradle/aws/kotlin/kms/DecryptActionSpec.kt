package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.DecryptRequest
import aws.sdk.kotlin.services.kms.model.DecryptResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.kms.MockKmsClientInfoInternal
import com.kelvsyc.gradle.plugins.KmsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class DecryptActionSpec : FunSpec() {
    init {
        test("execute - sends ciphertext bytes and writes returned plaintext") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(KmsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockKmsClientInfo::class, MockKmsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockKmsClientInfo>("mock") {}

            val client = extension.getClient<KmsClient, MockKmsClientInfo>("mock").get()!!
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
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
