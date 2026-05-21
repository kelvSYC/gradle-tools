package com.kelvsyc.gradle.aws.java.appconfig

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.appconfig.AppConfigClient
import software.amazon.awssdk.services.appconfig.model.DeploymentState
import software.amazon.awssdk.services.appconfig.model.GetDeploymentRequest
import software.amazon.awssdk.services.appconfig.model.GetDeploymentResponse

class WaitForDeploymentActionSpec : FunSpec() {
    init {
        test("execute - returns normally when deployment reaches COMPLETE state") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)

            val response = mockk<GetDeploymentResponse>()
            every { response.state() } returns DeploymentState.COMPLETE
            every { client.getDeployment(any<GetDeploymentRequest>()) } returns response

            val params = project.objects.newInstance<WaitForDeploymentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.deploymentNumber.set(7)
            params.pollIntervalMs.set(0L)
            params.maxWaitTimeMs.set(30_000L)

            val action = object : WaitForDeploymentAction() {
                override fun getParameters() = params
            }
            action.execute() // should not throw
        }

        test("execute - throws GradleException when deployment is ROLLED_BACK") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)

            val response = mockk<GetDeploymentResponse>()
            every { response.state() } returns DeploymentState.ROLLED_BACK
            every { client.getDeployment(any<GetDeploymentRequest>()) } returns response

            val params = project.objects.newInstance<WaitForDeploymentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.deploymentNumber.set(7)
            params.pollIntervalMs.set(0L)
            params.maxWaitTimeMs.set(30_000L)

            val action = object : WaitForDeploymentAction() {
                override fun getParameters() = params
            }
            shouldThrow<GradleException> { action.execute() }
        }

        test("execute - throws GradleException when timeout is exceeded") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)

            val response = mockk<GetDeploymentResponse>()
            every { response.state() } returns DeploymentState.DEPLOYING
            every { client.getDeployment(any<GetDeploymentRequest>()) } returns response

            val params = project.objects.newInstance<WaitForDeploymentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.deploymentNumber.set(7)
            params.pollIntervalMs.set(0L)
            params.maxWaitTimeMs.set(0L)

            val action = object : WaitForDeploymentAction() {
                override fun getParameters() = params
            }
            shouldThrow<GradleException> { action.execute() }
        }
    }
}
