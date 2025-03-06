package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import org.jfrog.artifactory.client.Artifactory

interface ArtifactoryClientInfo : ServiceClientInfo<Artifactory> {
    val url: Property<String>
    val credentials: Property<PasswordCredentials>
}
