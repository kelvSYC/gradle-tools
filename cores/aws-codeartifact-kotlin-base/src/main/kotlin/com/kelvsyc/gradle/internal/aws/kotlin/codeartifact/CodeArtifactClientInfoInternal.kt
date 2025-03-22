package com.kelvsyc.gradle.internal.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import com.kelvsyc.gradle.aws.kotlin.codeartifact.CodeArtifactClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class CodeArtifactClientInfoInternal : CodeArtifactClientInfo, ServiceClientInfoInternal<CodeartifactClient> {
    override fun createClient(): CodeartifactClient {
        return CodeartifactClient {
            if (this@CodeArtifactClientInfoInternal.region.isPresent) {
                region = this@CodeArtifactClientInfoInternal.region.get()
            }

            if (this@CodeArtifactClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@CodeArtifactClientInfoInternal.credentials.get()
            }
        }
    }
}
