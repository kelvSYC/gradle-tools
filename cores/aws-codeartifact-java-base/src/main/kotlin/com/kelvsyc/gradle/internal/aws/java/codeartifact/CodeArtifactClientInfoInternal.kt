package com.kelvsyc.gradle.internal.aws.java.codeartifact

import com.kelvsyc.gradle.aws.java.codeartifact.CodeArtifactClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.codeartifact.CodeartifactClient

abstract class CodeArtifactClientInfoInternal : CodeArtifactClientInfo, ServiceClientInfoInternal<CodeartifactClient> {
    override fun createClient(): CodeartifactClient {
        return CodeartifactClient.builder().apply {
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
