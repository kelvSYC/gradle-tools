package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class KmsClientBuildServiceSpec : FunSpec() {
    init {
        test("getClient - returns the mock client") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<KeyManagementServiceClient>()
            MockKmsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("kms", MockKmsClientBuildService::class) {}
                .get()

            service.getClient() shouldBe client
        }
    }
}
