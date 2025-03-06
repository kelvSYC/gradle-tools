package com.kelvsyc.gradle.internal.artifactory

import com.kelvsyc.gradle.artifactory.ArtifactoryClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

abstract class ArtifactoryClientInfoInternal : ArtifactoryClientInfo, ServiceClientInfoInternal<Artifactory> {
    override fun createClient(): Artifactory {
        return ArtifactoryClientBuilder.create().apply {
            url = this@ArtifactoryClientInfoInternal.url.get()
            username = credentials.get().username
            password = credentials.get().password
        }.build()
    }
}
