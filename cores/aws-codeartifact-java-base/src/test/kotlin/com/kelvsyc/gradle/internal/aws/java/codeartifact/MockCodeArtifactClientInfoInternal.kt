package com.kelvsyc.gradle.internal.aws.java.codeartifact

import com.kelvsyc.gradle.aws.java.codeartifact.MockCodeArtifactClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.codeartifact.CodeartifactClient

abstract class MockCodeArtifactClientInfoInternal : MockCodeArtifactClientInfo, ServiceClientInfoInternal<CodeartifactClient> {
    override fun createClient(): CodeartifactClient = mockk()
}
