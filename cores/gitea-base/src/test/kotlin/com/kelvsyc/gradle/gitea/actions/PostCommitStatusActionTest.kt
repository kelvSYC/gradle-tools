package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.gitea.MockGiteaBearerClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.CommitStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class PostCommitStatusActionTest : FunSpec() {
    init {
        test("execute - sends correct status fields") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val bodySlot = slot<Map<String, String?>>()
            val call = mockk<Call<CommitStatus>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createStatus("myowner", "myrepo", "abc123", capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<PostCommitStatusAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.sha.set("abc123")
            params.state.set("success")
            params.context.set("ci/build")
            params.targetUrl.set("https://ci.example.com/builds/42")
            params.description.set("All checks passed")

            val action = object : PostCommitStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body["state"] shouldBe "success"
            body["context"] shouldBe "ci/build"
            body["target_url"] shouldBe "https://ci.example.com/builds/42"
            body["description"] shouldBe "All checks passed"
        }

        test("execute - omits optional fields when absent") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val bodySlot = slot<Map<String, String?>>()
            val call = mockk<Call<CommitStatus>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createStatus("myowner", "myrepo", "abc123", capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<PostCommitStatusAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.sha.set("abc123")
            params.state.set("pending")
            params.context.set("ci/lint")

            val action = object : PostCommitStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body["state"] shouldBe "pending"
            body["context"] shouldBe "ci/lint"
            body.containsKey("target_url") shouldBe false
            body.containsKey("description") shouldBe false
        }
    }
}

