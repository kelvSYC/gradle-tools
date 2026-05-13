package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Project
import com.kelvsyc.gradle.bitbucket.server.model.Repository
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
            val client = mockk<BitbucketServerService>()
            MockBitbucketServerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketServerClientBuildService::class)

            val repo = Repository(
                id = 1,
                slug = "my-repo",
                name = "My Repo",
                project = Project(key = "PROJ", name = "My Project"),
                forkable = true,
            )
            val call = mockk<Call<Repository>>()
            every { call.execute() } returns Response.success(repo)
            every { client.getRepository("PROJ", "my-repo") } returns call

            val provider = project.providers.ofKt(GetRepositoryValueSource::class) {
                parameters.service.set(service)
                parameters.projectKey.set("PROJ")
                parameters.repoSlug.set("my-repo")
            }
            val result = provider.get()

            result.slug shouldBe "my-repo"
            result.name shouldBe "My Repo"
            result.project?.key shouldBe "PROJ"
            result.forkable shouldBe true
        }
    }
}
