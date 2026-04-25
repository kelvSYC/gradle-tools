package com.kelvsyc.gradle.internal.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.secretsmanager.MockSecretsManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

abstract class MockSecretsManagerClientInfoInternal : MockSecretsManagerClientInfo, ServiceClientInfoInternal<SecretsManagerClient> {
    override fun createClient(): SecretsManagerClient = mockk()
}
