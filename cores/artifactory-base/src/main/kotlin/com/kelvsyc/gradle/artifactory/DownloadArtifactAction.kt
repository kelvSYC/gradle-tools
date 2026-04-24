package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jfrog.artifactory.client.Artifactory

/**
 * Gradle [WorkAction] for downloading a single artifact from an Artifactory repository.
 *
 * This action is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic repositories).
 * For Maven or Ivy repositories, prefer Gradle's built-in dependency resolution instead.
 */
abstract class DownloadArtifactAction : WorkAction<DownloadArtifactAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val repository: Property<String>
        val path: Property<String>

        val outputFile: RegularFileProperty
    }

    private val client: Provider<Artifactory> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val inputStream = client.get()
            .repository(parameters.repository.get())
            .download(parameters.path.get())
            .doDownload()

        inputStream.use { input ->
            parameters.outputFile.get().asFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
