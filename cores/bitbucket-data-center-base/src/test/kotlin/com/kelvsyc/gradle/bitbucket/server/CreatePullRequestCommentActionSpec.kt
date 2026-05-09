package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Comment
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.server.MockBitbucketServerClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketDataCenterBasePlugin
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
        test("execute - sends comment with correct text") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<Comment>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.createPullRequestComment("PROJ", "repo", 7, capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<CreatePullRequestCommentAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
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
