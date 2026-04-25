package com.kelvsyc.gradle.internal.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.artifact.MockArtifactRegistryClientInfo
import io.mockk.mockk

abstract class MockArtifactRegistryClientInfoInternal : MockArtifactRegistryClientInfo, ServiceClientInfoInternal<ArtifactRegistryClient> {
    override fun createClient(): ArtifactRegistryClient = mockk(relaxed = true)
}
