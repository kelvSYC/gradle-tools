package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueResponse
import aws.sdk.kotlin.services.secretsmanager.model.SecretValueEntry
import aws.sdk.kotlin.services.secretsmanager.paginators.batchGetSecretValuePaginated
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SecretBatchValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of secret names to values") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("sm", MockSecretsManagerClientBuildService::class)
            val requestSlot = slot<BatchGetSecretValueRequest>()

            mockkStatic("aws.sdk.kotlin.services.secretsmanager.paginators.PaginatorsKt")
            every { client.batchGetSecretValuePaginated(capture(requestSlot)) } returns flowOf(
                BatchGetSecretValueResponse {
                    secretValues = listOf(
                        SecretValueEntry { name = "secret-one"; secretString = "value-one" },
                        SecretValueEntry { name = "secret-two"; secretString = "value-two" },
                    )
                }
            )

            val provider = project.providers.ofKt(SecretBatchValueSource::class) {
                parameters.service.set(service)
                parameters.secretIds.set(setOf("secret-one", "secret-two"))
            }
            val result = provider.get()

            result shouldHaveSize 2
            result shouldContain ("secret-one" to "value-one")
            result shouldContain ("secret-two" to "value-two")
            requestSlot.captured.secretIdList!! shouldContain "secret-one"
            requestSlot.captured.secretIdList!! shouldContain "secret-two"
        }
    }
}
