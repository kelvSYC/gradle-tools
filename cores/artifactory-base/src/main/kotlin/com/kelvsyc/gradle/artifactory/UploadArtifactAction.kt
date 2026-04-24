package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jfrog.artifactory.client.Artifactory

/**
 * Gradle [WorkAction] for uploading a single artifact to an Artifactory repository.
 *
 * This action is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic repositories).
 * For Maven or Ivy repositories, prefer Gradle's built-in publishing mechanisms (e.g. `maven-publish`) instead.
 */
abstract class UploadArtifactAction : WorkAction<UploadArtifactAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val repository: Property<String>
        val path: Property<String>

        val inputFile: RegularFileProperty
    }

    private val client: Provider<Artifactory> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        client.get()
            .repository(parameters.repository.get())
            .upload(parameters.path.get(), parameters.inputFile.get().asFile)
            .doUpload()
    }
}
