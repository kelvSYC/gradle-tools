package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Account
import com.kelvsyc.gradle.bitbucket.cloud.model.Branch
import com.kelvsyc.gradle.bitbucket.cloud.model.Repository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetRepositoryValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns repository metadata") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val repo = Repository(
                fullName = "myworkspace/my-repo",
                name = "my-repo",
                slug = "my-repo",
                isPrivate = true,
                mainBranch = Branch(name = "main"),
                owner = Account(displayName = "My Workspace"),
            )
            val call = mockk<Call<Repository>>()
            every { call.execute() } returns Response.success(repo)
            every { client.getRepository("myworkspace", "my-repo") } returns call

            val provider = project.providers.ofKt(GetRepositoryValueSource::class) {
                parameters.service.set(service)
                parameters.workspace.set("myworkspace")
                parameters.repoSlug.set("my-repo")
            }
            val result = provider.get()

            result.fullName shouldBe "myworkspace/my-repo"
            result.isPrivate shouldBe true
            result.mainBranch?.name shouldBe "main"
            result.owner?.displayName shouldBe "My Workspace"
        }
    }
}
