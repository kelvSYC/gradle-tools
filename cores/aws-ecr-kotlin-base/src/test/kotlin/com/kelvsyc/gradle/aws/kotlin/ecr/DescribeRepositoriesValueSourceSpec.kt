package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesRequest
import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesResponse
import aws.sdk.kotlin.services.ecr.model.Repository
import aws.sdk.kotlin.services.ecr.paginators.describeRepositoriesPaginated
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ecr.MockEcrClientInfoInternal
import com.kelvsyc.gradle.plugins.EcrKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class DescribeRepositoriesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of repository names to URIs") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(EcrKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockEcrClientInfo::class, MockEcrClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockEcrClientInfo>("mock") {}
            val client = extension.getClient<EcrClient, MockEcrClientInfo>("mock").get()!!

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

            val provider = project.providers.of(DescribeRepositoriesValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("repo-one" to "123.dkr.ecr.us-east-1.amazonaws.com/repo-one")
            result shouldContain ("repo-two" to "123.dkr.ecr.us-east-1.amazonaws.com/repo-two")
        }
    }
}
