package com.kelvsyc.gradle.google.cloud.artifact

import com.google.api.gax.core.CredentialsProvider
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

interface ArtifactRegistryClientInfo : ServiceClientInfo<ArtifactRegistryClient> {
    val credentials: Property<CredentialsProvider>
}
