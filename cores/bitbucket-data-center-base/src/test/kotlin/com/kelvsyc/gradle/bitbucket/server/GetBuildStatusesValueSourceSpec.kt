package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.BuildStatus
import com.kelvsyc.gradle.bitbucket.server.model.PaginatedResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.bitbucket.server.MockBitbucketServerClientInfoInternal
import com.kelvsyc.gradle.plugins.BitbucketDataCenterBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import retrofit2.Call
import retrofit2.Response

class GetBuildStatusesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns all statuses across pages") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(BitbucketDataCenterBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(
                MockBitbucketServerClientInfo::class,
                MockBitbucketServerClientInfoInternal::class,
            )
            extension.service.get().registerIfAbsent<MockBitbucketServerClientInfo>("mock") {}
            val client = extension.getClient<BitbucketServerService, _>("mock").get()

            val page1 = PaginatedResponse(
                values = listOf(BuildStatus(key = "build-1", state = "SUCCESSFUL")),
                isLastPage = false,
                nextPageStart = 1,
            )
            val page2 = PaginatedResponse(
                values = listOf(BuildStatus(key = "build-2", state = "FAILED")),
                isLastPage = true,
            )
            val call1 = mockk<Call<PaginatedResponse<BuildStatus>>>()
            every { call1.execute() } returns Response.success(page1)
            every { client.getBuildStatuses("abc123", start = 0, limit = null) } returns call1

            val call2 = mockk<Call<PaginatedResponse<BuildStatus>>>()
            every { call2.execute() } returns Response.success(page2)
            every { client.getBuildStatuses("abc123", start = 1, limit = null) } returns call2

            val provider = project.providers.of(GetBuildStatusesValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.commitId.set("abc123")
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
