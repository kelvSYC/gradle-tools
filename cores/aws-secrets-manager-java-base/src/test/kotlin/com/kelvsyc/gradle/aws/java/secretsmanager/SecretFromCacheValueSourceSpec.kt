package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SecretFromCacheValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string from cache") {
            val project = ProjectBuilder.builder().build()
            val cache = mockk<SecretCache>()
            MockSecretCacheBuildService.mockClient = cache
            val service = project.gradle.sharedServices.registerIfAbsent(
                "sm-cache",
                MockSecretCacheBuildService::class
            )
            every { cache.getSecretString("my-secret") } returns "cached-value"

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(SecretFromCacheValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "cached-value"
        }
    }
}
