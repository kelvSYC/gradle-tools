package com.kelvsyc.gradle.internal.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import com.kelvsyc.gradle.aws.kotlin.codeartifact.MockCodeArtifactClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockCodeArtifactClientInfoInternal : MockCodeArtifactClientInfo, ServiceClientInfoInternal<CodeartifactClient> {
    override fun createClient(): CodeartifactClient = mockk()
}
