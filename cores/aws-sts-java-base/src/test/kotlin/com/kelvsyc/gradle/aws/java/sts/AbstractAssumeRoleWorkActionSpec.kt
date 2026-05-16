package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.aws.java.AwsSessionCredential
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse
import software.amazon.awssdk.services.sts.model.Credentials
import java.time.Instant

class AbstractAssumeRoleWorkActionSpec : FunSpec() {
    init {
        test("execute - passes roleArn, roleSessionName, duration to request and calls doExecute with credential") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)

            val expiration = Instant.parse("2099-01-01T00:00:00Z")
            val sdkCreds = Credentials.builder()
                .accessKeyId("AKIAIOSFODNN7EXAMPLE")
                .secretAccessKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
                .sessionToken("AQoDYXdzEJr...")
                .expiration(expiration)
                .build()

            val requestSlot = slot<AssumeRoleRequest>()
            val response = mockk<AssumeRoleResponse>()
            every { response.credentials() } returns sdkCreds
            every { client.assumeRole(capture(requestSlot)) } returns response

            val params = project.objects.newInstance<AbstractAssumeRoleWorkAction.Parameters>()
            params.service.set(service)
            params.roleArn.set("arn:aws:iam::123456789012:role/MyRole")
            params.roleSessionName.set("gradle-build")
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
            captured.roleArn() shouldBe "arn:aws:iam::123456789012:role/MyRole"
            captured.roleSessionName() shouldBe "gradle-build"
            captured.durationSeconds() shouldBe 3600

            capturedCredential shouldBe AwsSessionCredential(
                accessKeyId = "AKIAIOSFODNN7EXAMPLE",
                secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                sessionToken = "AQoDYXdzEJr...",
                expiration = expiration,
            )
        }

        test("execute - includes externalId in request when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)

            val expiration = Instant.parse("2099-01-01T00:00:00Z")
            val sdkCreds = Credentials.builder()
                .accessKeyId("AKIAIOSFODNN7EXAMPLE")
                .secretAccessKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
                .sessionToken("AQoDYXdzEJr...")
                .expiration(expiration)
                .build()

            val requestSlot = slot<AssumeRoleRequest>()
            val response = mockk<AssumeRoleResponse>()
            every { response.credentials() } returns sdkCreds
            every { client.assumeRole(capture(requestSlot)) } returns response

            val params = project.objects.newInstance<AbstractAssumeRoleWorkAction.Parameters>()
            params.service.set(service)
            params.roleArn.set("arn:aws:iam::123456789012:role/CrossAccountRole")
            params.roleSessionName.set("gradle-build")
            params.duration.set(3600L)
            params.externalId.set("cross-account-external-id")

            val action = object : AbstractAssumeRoleWorkAction() {
                override fun getParameters() = params
                override fun doExecute(credential: AwsSessionCredential) = Unit
            }
            action.execute()

            val captured = requestSlot.captured
            captured.externalId() shouldBe "cross-account-external-id"
        }
    }
}
