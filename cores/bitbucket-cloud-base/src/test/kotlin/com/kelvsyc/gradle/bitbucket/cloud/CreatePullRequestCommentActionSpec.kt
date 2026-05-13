package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequestComment
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

class CreatePullRequestCommentActionSpec : FunSpec() {
    init {
        test("execute - sends comment with correct content structure") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<PullRequestComment>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createPullRequestComment("ws", "repo", 42, capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<CreatePullRequestCommentAction.Parameters>()
            params.service.set(service)
            params.workspace.set("ws")
            params.repoSlug.set("repo")
            params.pullRequestId.set(42L)
            params.body.set("Build **passed** :white_check_mark:")

            val action = object : CreatePullRequestCommentAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            @Suppress("UNCHECKED_CAST")
            val content = body["content"] as Map<String, Any>
            content["raw"] shouldBe "Build **passed** :white_check_mark:"
        }
    }
}
