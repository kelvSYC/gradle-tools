package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.ecr.MockEcrClientInfoInternal
import com.kelvsyc.gradle.plugins.EcrJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse
import software.amazon.awssdk.services.ecr.model.Repository
import software.amazon.awssdk.services.ecr.paginators.DescribeRepositoriesIterable
import java.util.stream.Stream

class DescribeRepositoriesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of repository names to URIs") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(EcrJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockEcrClientInfo::class, MockEcrClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockEcrClientInfo>("mock") {}
            val client = extension.getClient<EcrClient, _>("mock").get()

            val repo1 = mockk<Repository>()
            every { repo1.repositoryName() } returns "repo-one"
            every { repo1.repositoryUri() } returns "123.dkr.ecr.us-east-1.amazonaws.com/repo-one"

            val repo2 = mockk<Repository>()
            every { repo2.repositoryName() } returns "repo-two"
            every { repo2.repositoryUri() } returns "123.dkr.ecr.us-east-1.amazonaws.com/repo-two"

            val response = mockk<DescribeRepositoriesResponse>()
            every { response.repositories() } returns listOf(repo1, repo2)

            val paginator = mockk<DescribeRepositoriesIterable>()
            every { paginator.stream() } returns Stream.of(response)

            every { client.describeRepositoriesPaginator(any<DescribeRepositoriesRequest>()) } returns paginator

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
