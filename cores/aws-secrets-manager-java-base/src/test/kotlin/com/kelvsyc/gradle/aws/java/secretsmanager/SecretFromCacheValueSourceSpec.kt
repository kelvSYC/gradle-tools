package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.secretsmanager.MockSecretCacheClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SecretFromCacheValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string from cache") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretCacheClientInfo::class, MockSecretCacheClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretCacheClientInfo>("mock") {}
            val cache = extension.getClient<SecretCache, _>("mock").get()
            every { cache.getSecretString("my-secret") } returns "cached-value"

            val provider = project.providers.of(SecretFromCacheValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "cached-value"
        }
    }
}

