package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.gitea.MockGiteaBearerClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.Comment
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

class CreatePullRequestCommentActionTest : FunSpec() {
    init {
        test("execute - sends comment with correct content") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<GiteaService>()
            MockGiteaBearerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "gitea",
                MockGiteaBearerClientBuildService::class,
            )

            val bodySlot = slot<Map<String, String>>()
            val call = mockk<Call<Comment>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createComment("myowner", "myrepo", 42L, capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<CreatePullRequestCommentAction.Parameters>()
            params.service.set(service)
            params.owner.set("myowner")
            params.repo.set("myrepo")
            params.index.set(42L)
            params.body.set("Build **passed** with all checks green")

            val action = object : CreatePullRequestCommentAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body["body"] shouldBe "Build **passed** with all checks green"
        }
    }
}

