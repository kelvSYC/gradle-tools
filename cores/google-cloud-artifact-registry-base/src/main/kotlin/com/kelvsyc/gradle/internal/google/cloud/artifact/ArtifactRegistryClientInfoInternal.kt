package com.kelvsyc.gradle.internal.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ArtifactRegistrySettings
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.artifact.ArtifactRegistryClientInfo

abstract class ArtifactRegistryClientInfoInternal :
    ArtifactRegistryClientInfo, ServiceClientInfoInternal<ArtifactRegistryClient> {
    override fun createClient(): ArtifactRegistryClient {
        val settings = ArtifactRegistrySettings.newBuilder().apply {
            credentialsProvider = credentials.get()
        }.build()

        return ArtifactRegistryClient.create(settings)
    }
}
