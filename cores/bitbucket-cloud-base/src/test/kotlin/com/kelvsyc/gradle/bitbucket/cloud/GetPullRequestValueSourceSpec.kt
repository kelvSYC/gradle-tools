package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Account
import com.kelvsyc.gradle.bitbucket.cloud.model.Branch
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequest
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequestEndpoint
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
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val pr = PullRequest(
                id = 42,
                title = "Add feature X",
                state = "OPEN",
                source = PullRequestEndpoint(branch = Branch(name = "feature/x")),
                destination = PullRequestEndpoint(branch = Branch(name = "main")),
                author = Account(displayName = "Alice"),
            )
            val call = mockk<Call<PullRequest>>()
            every { call.execute() } returns Response.success(pr)
            every { client.getPullRequest("myworkspace", "my-repo", 42) } returns call

            val provider = project.providers.ofKt(GetPullRequestValueSource::class) {
                parameters.service.set(service)
                parameters.workspace.set("myworkspace")
                parameters.repoSlug.set("my-repo")
                parameters.pullRequestId.set(42L)
            }
            val result = provider.get()

            result.id shouldBe 42
            result.title shouldBe "Add feature X"
            result.state shouldBe "OPEN"
            result.source?.branch?.name shouldBe "feature/x"
            result.destination?.branch?.name shouldBe "main"
            result.author?.displayName shouldBe "Alice"
        }
    }
}
