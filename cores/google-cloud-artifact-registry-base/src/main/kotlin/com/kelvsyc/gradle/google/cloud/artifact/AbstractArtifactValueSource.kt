package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.FileName
import com.google.devtools.artifactregistry.v1.GetFileRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Base class for [ValueSource] implementations that provide a value by reading a file from Google Artifact Registry.
 *
 * Subclasses should implement the [doObtain] function, transforming an [InputStream] to an object of the desired type.
 */
abstract class AbstractArtifactValueSource<T, P : AbstractArtifactValueSource.Parameters> : ValueSource<T, P> {
    interface Parameters : ValueSourceParameters {
        val client: Property<ArtifactRegistryClient>

        val projectName: Property<String>
        val location: Property<String>
        val repository: Property<String>
        val filename: Property<String>
    }

    abstract fun doObtain(input: InputStream): T?

    override fun obtain(): T? {
        val fileInternal = FileName.newBuilder().apply {
            project = parameters.projectName.get()
            location = parameters.location.get()
            repository = parameters.repository.get()
            file = parameters.filename.get()
        }.build()

        val request = GetFileRequest.newBuilder().apply {
            name = fileInternal.toString()
        }.build()

        return runBlocking {
            val out = PipedOutputStream()
            val input = PipedInputStream(out)

            val job = launch {
                val response = parameters.client.get().getFile(request)
                response.writeTo(out)
            }
            val result = doObtain(input)

            result.also {
                // Make sure we are done downloading
                job.join()
            }
        }
    }
}
