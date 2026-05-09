package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.PullRequest
import com.kelvsyc.gradle.bitbucket.server.model.PullRequestParticipant
import com.kelvsyc.gradle.bitbucket.server.model.PullRequestRef
import com.kelvsyc.gradle.bitbucket.server.model.User
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.server.MockBitbucketServerClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketDataCenterBasePlugin
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
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

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

            val provider = project.providers.of(GetPullRequestValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
