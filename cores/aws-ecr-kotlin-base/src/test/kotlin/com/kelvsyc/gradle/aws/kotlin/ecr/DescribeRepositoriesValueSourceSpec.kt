package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesRequest
import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesResponse
import aws.sdk.kotlin.services.ecr.model.Repository
import aws.sdk.kotlin.services.ecr.paginators.describeRepositoriesPaginated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DescribeRepositoriesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of repository names to URIs") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)

            mockkStatic("aws.sdk.kotlin.services.ecr.paginators.PaginatorsKt")
            every { client.describeRepositoriesPaginated(any<DescribeRepositoriesRequest>()) } returns flowOf(
                DescribeRepositoriesResponse {
                    repositories = listOf(
                        Repository {
                            repositoryName = "repo-one"
                            repositoryUri = "123.dkr.ecr.us-east-1.amazonaws.com/repo-one"
                        },
                        Repository {
                            repositoryName = "repo-two"
                            repositoryUri = "123.dkr.ecr.us-east-1.amazonaws.com/repo-two"
                        },
                    )
                }
            )

            val provider = project.providers.ofKt(DescribeRepositoriesValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("repo-one" to "123.dkr.ecr.us-east-1.amazonaws.com/repo-one")
            result shouldContain ("repo-two" to "123.dkr.ecr.us-east-1.amazonaws.com/repo-two")
        }
    }
}
