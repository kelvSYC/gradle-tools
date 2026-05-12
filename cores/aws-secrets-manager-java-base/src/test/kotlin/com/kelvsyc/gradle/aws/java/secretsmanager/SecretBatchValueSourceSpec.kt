package com.kelvsyc.gradle.aws.java.secretsmanager

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain as collectionShouldContain
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.BatchGetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.BatchGetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.SecretValueEntry
import software.amazon.awssdk.services.secretsmanager.paginators.BatchGetSecretValueIterable
import java.util.stream.Stream

class SecretBatchValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of secret names to values") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "sm",
                MockSecretsManagerClientBuildService::class
            )
            val requestSlot = slot<BatchGetSecretValueRequest>()

            val entry1 = mockk<SecretValueEntry>()
            every { entry1.name() } returns "secret-one"
            every { entry1.secretString() } returns "value-one"

            val entry2 = mockk<SecretValueEntry>()
            every { entry2.name() } returns "secret-two"
            every { entry2.secretString() } returns "value-two"

            val response = mockk<BatchGetSecretValueResponse>()
            every { response.secretValues() } returns listOf(entry1, entry2)

            val paginator = mockk<BatchGetSecretValueIterable>()
            every { paginator.stream() } returns Stream.of(response)

            every { client.batchGetSecretValuePaginator(capture(requestSlot)) } returns paginator

            val provider = project.providers.ofKt(SecretBatchValueSource::class) {
                parameters.service.set(service)
                parameters.secretIds.set(setOf("secret-one", "secret-two"))
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("secret-one" to "value-one")
            result shouldContain ("secret-two" to "value-two")
            requestSlot.captured.secretIdList() collectionShouldContain "secret-one"
            requestSlot.captured.secretIdList() collectionShouldContain "secret-two"
        }
    }
}
