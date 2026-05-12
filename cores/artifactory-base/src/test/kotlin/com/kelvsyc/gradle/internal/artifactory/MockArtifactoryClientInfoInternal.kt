package com.kelvsyc.gradle.internal.artifactory

import com.kelvsyc.gradle.artifactory.MockArtifactoryClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import org.jfrog.artifactory.client.Artifactory

abstract class MockArtifactoryClientInfoInternal : MockArtifactoryClientInfo, ServiceClientInfoInternal<Artifactory> {
    override fun createClient(): Artifactory = mockk()
}
