package com.kelvsyc.gradle.internal.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import com.kelvsyc.gradle.aws.kotlin.secretsmanager.MockSecretsManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSecretsManagerClientInfoInternal : MockSecretsManagerClientInfo, ServiceClientInfoInternal<SecretsManagerClient> {
    override fun createClient(): SecretsManagerClient = mockk()
}
