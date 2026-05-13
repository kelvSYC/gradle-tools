package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Comment
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
        test("execute - sends comment with correct text") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketServerService>()
            MockBitbucketServerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketServerClientBuildService::class)

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<Comment>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createPullRequestComment("PROJ", "repo", 7, capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<CreatePullRequestCommentAction.Parameters>()
            params.service.set(service)
            params.projectKey.set("PROJ")
            params.repoSlug.set("repo")
            params.pullRequestId.set(7L)
            params.text.set("Build **passed**")

            val action = object : CreatePullRequestCommentAction() {
                override fun getParameters() = params
            }
            action.execute()

            bodySlot.captured["text"] shouldBe "Build **passed**"
        }
    }
}
