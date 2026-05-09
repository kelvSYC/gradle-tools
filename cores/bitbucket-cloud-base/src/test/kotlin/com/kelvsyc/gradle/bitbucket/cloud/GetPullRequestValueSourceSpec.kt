package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Account
import com.kelvsyc.gradle.bitbucket.cloud.model.Branch
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequest
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequestEndpoint
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.cloud.MockBitbucketCloudClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketCloudBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetPullRequestValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns pull request metadata") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketCloudBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketCloudClientInfo::class,
                MockBitbucketCloudClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketCloudClientInfo>("mock") {}
            val client = extension.getClient<BitbucketCloudService, _>("mock").get()

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

            val provider = project.providers.of(GetPullRequestValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
