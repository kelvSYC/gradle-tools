package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequestComment
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.cloud.MockBitbucketCloudClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketCloudBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class CreatePullRequestCommentActionSpec : FunSpec() {
    init {
        test("execute - sends comment with correct content structure") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketCloudBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketCloudClientInfo::class,
                MockBitbucketCloudClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketCloudClientInfo>("mock") {}
            val client = extension.getClient<BitbucketCloudService, _>("mock").get()

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<PullRequestComment>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createPullRequestComment("ws", "repo", 42, capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<CreatePullRequestCommentAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
