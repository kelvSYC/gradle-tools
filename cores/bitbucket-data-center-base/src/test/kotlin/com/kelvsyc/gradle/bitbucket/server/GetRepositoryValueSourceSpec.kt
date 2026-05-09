package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Project
import com.kelvsyc.gradle.bitbucket.server.model.Repository
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

class GetRepositoryValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns repository metadata") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

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

            val provider = project.providers.of(GetRepositoryValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
