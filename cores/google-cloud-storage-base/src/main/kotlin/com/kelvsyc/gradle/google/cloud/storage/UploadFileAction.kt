package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.nio.ByteBuffer

/**
 * Gradle [WorkAction] for uploading a file to Google Cloud Storage.
 */
abstract class UploadFileAction : WorkAction<UploadFileAction.Parameters> {
    companion object {
        const val BUFFER_SIZE = 1024 * 1024
    }

    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val bucket: Property<String>
        val blobName: Property<String>

        val inputFile: RegularFileProperty
    }

    private val client: Provider<Storage> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val blobId = BlobId.of(parameters.bucket.get(), parameters.blobName.get())
        val blobInfo = BlobInfo.newBuilder(blobId).build()

        parameters.inputFile.get().asFile.inputStream().use { inputStream ->
            client.get().writer(blobInfo).use { writer ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    writer.write(ByteBuffer.wrap(buffer, 0, bytesRead))
                }
            }
        }
    }
}
