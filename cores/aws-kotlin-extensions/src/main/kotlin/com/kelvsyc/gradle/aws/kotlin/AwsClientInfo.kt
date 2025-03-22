package com.kelvsyc.gradle.aws.kotlin

import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.client.SdkClient
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface AwsClientInfo<T : SdkClient> : ServiceClientInfo<T> {
    val region: Property<String>

    val credentials: Property<CredentialsProvider>
}
