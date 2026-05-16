package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.AssumeRoleRequest
import aws.sdk.kotlin.services.sts.model.AssumeRoleResponse
import aws.sdk.kotlin.services.sts.model.Credentials
import aws.smithy.kotlin.runtime.time.Instant as SmithyInstant
import com.kelvsyc.gradle.aws.kotlin.AwsSessionCredential
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractAssumeRoleWorkActionSpec : FunSpec() {
    init {
        test("execute - passes roleArn, roleSessionName, duration and calls doExecute with credential") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            val requestSlot = slot<AssumeRoleRequest>()

            val smithyExpiration = SmithyInstant.fromEpochSeconds(4070908800L)
            val sdkCreds = Credentials {
                accessKeyId = "AKIAIOSFODNN7EXAMPLE"
                secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
                sessionToken = "AQoDYXdzEJr..."
                expiration = smithyExpiration
            }
            val response = mockk<AssumeRoleResponse>()
            every { response.credentials } returns sdkCreds
            coEvery { client.assumeRole(capture(requestSlot)) } returns response

            val params = project.objects.newInstance<AbstractAssumeRoleWorkAction.Parameters>()
            params.service.set(service)
            params.roleArn.set("arn:aws:iam::123456789012:role/TestRole")
            params.roleSessionName.set("test-session")
            params.duration.set(3600L)

            var capturedCredential: AwsSessionCredential? = null
            val action = object : AbstractAssumeRoleWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: AwsSessionCredential) {
                    capturedCredential = credential
                }
            }
            action.execute()

            val captured = requestSlot.captured
            captured.roleArn shouldBe "arn:aws:iam::123456789012:role/TestRole"
            captured.roleSessionName shouldBe "test-session"
            captured.durationSeconds shouldBe 3600L

            capturedCredential?.accessKeyId shouldBe "AKIAIOSFODNN7EXAMPLE"
            capturedCredential?.secretAccessKey shouldBe "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
            capturedCredential?.sessionToken shouldBe "AQoDYXdzEJr..."
            capturedCredential?.expiration shouldBe Instant.ofEpochSecond(4070908800L)
        }

        test("execute - includes externalId when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            val requestSlot = slot<AssumeRoleRequest>()

            val smithyExpiration = SmithyInstant.fromEpochSeconds(4070908800L)
            val sdkCreds = Credentials {
                accessKeyId = "AKIAIOSFODNN7EXAMPLE"
                secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
                sessionToken = "AQoDYXdzEJr..."
                expiration = smithyExpiration
            }
            val response = mockk<AssumeRoleResponse>()
            every { response.credentials } returns sdkCreds
            coEvery { client.assumeRole(capture(requestSlot)) } returns response

            val params = project.objects.newInstance<AbstractAssumeRoleWorkAction.Parameters>()
            params.service.set(service)
            params.roleArn.set("arn:aws:iam::123456789012:role/TestRole")
            params.roleSessionName.set("test-session")
            params.duration.set(3600L)
            params.externalId.set("my-external-id")

            val action = object : AbstractAssumeRoleWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: AwsSessionCredential) = Unit
            }
            action.execute()

            requestSlot.captured.externalId shouldBe "my-external-id"
        }
    }
}
