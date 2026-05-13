package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.CommitStatus
import com.kelvsyc.gradle.bitbucket.cloud.model.PaginatedResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetCommitStatusesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns all statuses across pages") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BitbucketCloudService>()
            MockBitbucketCloudClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("bb", MockBitbucketCloudClientBuildService::class)

            val page1 = PaginatedResponse(
                values = listOf(CommitStatus(key = "build-1", state = "SUCCESSFUL")),
                next = "https://api.bitbucket.org/2.0/next-page",
            )
            val page2 = PaginatedResponse(
                values = listOf(CommitStatus(key = "build-2", state = "FAILED")),
                next = null,
            )
            val call1 = mockk<Call<PaginatedResponse<CommitStatus>>>()
            every { call1.execute() } returns Response.success(page1)
            every { client.getCommitStatuses("myworkspace", "my-repo", "abc123") } returns call1

            val call2 = mockk<Call<PaginatedResponse<CommitStatus>>>()
            every { call2.execute() } returns Response.success(page2)
            every { client.getCommitStatusesPage("https://api.bitbucket.org/2.0/next-page") } returns call2

            val provider = project.providers.ofKt(GetCommitStatusesValueSource::class) {
                parameters.service.set(service)
                parameters.workspace.set("myworkspace")
                parameters.repoSlug.set("my-repo")
                parameters.commit.set("abc123")
            }
            val result = provider.get()

            result shouldHaveSize 2
            result[0].key shouldBe "build-1"
            result[0].state shouldBe "SUCCESSFUL"
            result[1].key shouldBe "build-2"
            result[1].state shouldBe "FAILED"
        }
    }
}
