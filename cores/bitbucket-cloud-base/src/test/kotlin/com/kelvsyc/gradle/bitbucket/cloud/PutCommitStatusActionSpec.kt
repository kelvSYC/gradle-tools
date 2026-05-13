package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.CommitStatus
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

class PutCommitStatusActionSpec : FunSpec() {
    init {
        test("execute - sends correct status fields") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<CommitStatus>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.putCommitStatus("ws", "repo", "abc123", "my-build", capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<PutCommitStatusAction.Parameters>()
            params.service.set(service)
            params.workspace.set("ws")
            params.repoSlug.set("repo")
            params.commit.set("abc123")
            params.key.set("my-build")
            params.state.set("SUCCESSFUL")
            params.url.set("https://ci.example.com/builds/42")
            params.name.set("Build #42")
            params.description.set("All tests passed")

            val action = object : PutCommitStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body["state"] shouldBe "SUCCESSFUL"
            body["key"] shouldBe "my-build"
            body["url"] shouldBe "https://ci.example.com/builds/42"
            body["name"] shouldBe "Build #42"
            body["description"] shouldBe "All tests passed"
        }

        test("execute - omits optional fields when absent") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val bodySlot = slot<Map<String, Any>>()
            val call = mockk<Call<CommitStatus>>()
            every { call.execute() } returns Response.success(mockk())
            every {
                client.putCommitStatus("ws", "repo", "abc123", "my-build", capture(bodySlot))
            } returns call

            val params = project.objects.newInstance<PutCommitStatusAction.Parameters>()
            params.service.set(service)
            params.workspace.set("ws")
            params.repoSlug.set("repo")
            params.commit.set("abc123")
            params.key.set("my-build")
            params.state.set("INPROGRESS")
            params.url.set("https://ci.example.com/builds/42")

            val action = object : PutCommitStatusAction() {
                override fun getParameters() = params
            }
            action.execute()

            val body = bodySlot.captured
            body.containsKey("name") shouldBe false
            body.containsKey("description") shouldBe false
        }
    }
}
