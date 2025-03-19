package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface S3ClientInfo : ServiceClientInfo<S3Client> {
    val region: Property<String>

    val credentials: Property<CredentialsProvider>
}
