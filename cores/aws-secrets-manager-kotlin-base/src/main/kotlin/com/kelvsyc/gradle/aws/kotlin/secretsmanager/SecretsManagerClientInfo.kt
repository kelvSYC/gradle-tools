package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface SecretsManagerClientInfo : ServiceClientInfo<SecretsManagerClient> {
    val region: Property<String>

    val credentials: Property<CredentialsProvider>
}
