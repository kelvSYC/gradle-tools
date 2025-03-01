package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient

interface CodeArtifactAsyncClientInfo : ServiceClientInfo<CodeartifactAsyncClient> {
    /**
     * The AWS region that this client communicates with.
     *
     * Leave unset to identify the region based on
     * [DefaultAwsRegionProviderChain][software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain],
     * the default used by the AWS Java SDK.
     */
    val region: Property<Region>

    /**
     * The credentials to authenticate with AWS.
     *
     * If absent, an [AnonymousCredentialsProvider][software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider]
     * will be used to authenticate with AWS. Use a
     * [DefaultCredentialsProvider][software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider]
     * to use the default credentials used by the AWS Java SDK.
     */
    val credentials: Property<AwsCredentialsProvider>
}
