package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueResponse
import aws.sdk.kotlin.services.secretsmanager.model.SecretValueEntry
import aws.sdk.kotlin.services.secretsmanager.paginators.batchGetSecretValuePaginated
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.secretsmanager.MockSecretsManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SecretBatchValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of secret names to values") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}
            val requestSlot = slot<BatchGetSecretValueRequest>()
            val client = extension.getClient<SecretsManagerClient, MockSecretsManagerClientInfo>("mock").get()!!

            mockkStatic("aws.sdk.kotlin.services.secretsmanager.paginators.PaginatorsKt")
            every { client.batchGetSecretValuePaginated(capture(requestSlot)) } returns flowOf(
                BatchGetSecretValueResponse {
                    secretValues = listOf(
                        SecretValueEntry { name = "secret-one"; secretString = "value-one" },
                        SecretValueEntry { name = "secret-two"; secretString = "value-two" },
                    )
                }
            )

            val provider = project.providers.of(SecretBatchValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
