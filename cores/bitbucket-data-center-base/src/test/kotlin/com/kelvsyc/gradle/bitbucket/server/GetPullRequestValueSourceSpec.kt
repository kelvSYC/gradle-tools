package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.PullRequest
import com.kelvsyc.gradle.bitbucket.server.model.PullRequestParticipant
import com.kelvsyc.gradle.bitbucket.server.model.PullRequestRef
import com.kelvsyc.gradle.bitbucket.server.model.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetPullRequestValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns pull request metadata") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketServerService>()
            MockBitbucketServerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketServerClientBuildService::class)

            val pr = PullRequest(
                id = 7,
                title = "Add feature Y",
                state = "OPEN",
                fromRef = PullRequestRef(displayId = "feature/y"),
                toRef = PullRequestRef(displayId = "main"),
                author = PullRequestParticipant(
                    user = User(displayName = "Bob"),
                    role = "AUTHOR",
                ),
            )
            val call = mockk<Call<PullRequest>>()
            every { call.execute() } returns Response.success(pr)
            every { client.getPullRequest("PROJ", "my-repo", 7) } returns call

            val provider = project.providers.ofKt(GetPullRequestValueSource::class) {
                parameters.service.set(service)
                parameters.projectKey.set("PROJ")
                parameters.repoSlug.set("my-repo")
                parameters.pullRequestId.set(7L)
            }
            val result = provider.get()

            result.id shouldBe 7
            result.title shouldBe "Add feature Y"
            result.state shouldBe "OPEN"
            result.fromRef?.displayId shouldBe "feature/y"
            result.toRef?.displayId shouldBe "main"
            result.author?.user?.displayName shouldBe "Bob"
        }
    }
}
