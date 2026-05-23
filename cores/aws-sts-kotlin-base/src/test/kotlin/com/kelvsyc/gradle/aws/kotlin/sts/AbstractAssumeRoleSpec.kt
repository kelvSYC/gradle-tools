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
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

/**
 * Concrete implementation of [AbstractAssumeRole] for testing purposes.
 */
abstract class ConcreteAssumeRole : AbstractAssumeRole() {
    var capturedCredential: AwsSessionCredential? = null

    override fun doExecute(credential: AwsSessionCredential) {
        capturedCredential = credential
    }
}

class AbstractAssumeRoleSpec : FunSpec() {
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

            val task = project.tasks.create("abstractAssumeRole", ConcreteAssumeRole::class.java)
            task.service.set(service)
            task.roleArn.set("arn:aws:iam::123456789012:role/TestRole")
            task.roleSessionName.set("test-session")
            task.duration.set(3600L)
            task.execute()

            val captured = requestSlot.captured
            captured.roleArn shouldBe "arn:aws:iam::123456789012:role/TestRole"
            captured.roleSessionName shouldBe "test-session"
            captured.durationSeconds shouldBe 3600

            task.capturedCredential?.accessKeyId shouldBe "AKIAIOSFODNN7EXAMPLE"
            task.capturedCredential?.secretAccessKey shouldBe "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
            task.capturedCredential?.sessionToken shouldBe "AQoDYXdzEJr..."
            task.capturedCredential?.expiration shouldBe Instant.ofEpochSecond(4070908800L)
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

            val task = project.tasks.create("abstractAssumeRole", ConcreteAssumeRole::class.java)
            task.service.set(service)
            task.roleArn.set("arn:aws:iam::123456789012:role/TestRole")
            task.roleSessionName.set("test-session")
            task.duration.set(3600L)
            task.externalId.set("my-external-id")
            task.execute()

            requestSlot.captured.externalId shouldBe "my-external-id"
        }
    }
}
