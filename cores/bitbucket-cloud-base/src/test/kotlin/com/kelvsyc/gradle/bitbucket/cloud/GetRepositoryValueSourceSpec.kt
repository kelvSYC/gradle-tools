package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Account
import com.kelvsyc.gradle.bitbucket.cloud.model.Branch
import com.kelvsyc.gradle.bitbucket.cloud.model.Repository
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

class GetRepositoryValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns repository metadata") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketCloudBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketCloudClientInfo::class,
                MockBitbucketCloudClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketCloudClientInfo>("mock") {}
            val client = extension.getClient<BitbucketCloudService, _>("mock").get()

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

            val provider = project.providers.of(GetRepositoryValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
