package com.kelvsyc.gradle.internal.aws.java.codeartifact

import com.kelvsyc.gradle.aws.java.codeartifact.CodeArtifactAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient

abstract class CodeArtifactAsyncClientInfoInternal :
    CodeArtifactAsyncClientInfo, ServiceClientInfoInternal<CodeartifactAsyncClient> {
    override fun createClient(): CodeartifactAsyncClient {
        return CodeartifactAsyncClient.builder().apply {
            if (region.isPresent) {
                region(region.get())
            }
            if (credentials.isPresent) {
                credentialsProvider(credentials.get())
            } else {
                credentialsProvider(AnonymousCredentialsProvider.create())
            }
        }.build()
    }
}
