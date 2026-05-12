package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient

/**
 * Test-only [SecretsManagerClientBuildService] that returns a pre-supplied mock [SecretsManagerClient].
 */
abstract class MockSecretsManagerClientBuildService : SecretsManagerClientBuildService() {
    override fun createClient(): SecretsManagerClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SecretsManagerClient? = null
    }
}
