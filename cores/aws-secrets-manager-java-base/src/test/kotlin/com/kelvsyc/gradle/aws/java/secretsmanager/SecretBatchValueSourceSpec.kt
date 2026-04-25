package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.secretsmanager.MockSecretsManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain as collectionShouldContain
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
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
            project.pluginManager.apply(SecretsManagerJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}
            val requestSlot = slot<BatchGetSecretValueRequest>()
            val client = extension.getClient<SecretsManagerClient, _>("mock").get()

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

            val provider = project.providers.of(SecretBatchValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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

