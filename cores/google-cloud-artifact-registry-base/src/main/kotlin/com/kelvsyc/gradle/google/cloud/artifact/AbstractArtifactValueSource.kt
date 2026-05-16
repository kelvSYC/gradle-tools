package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.FileName
import com.google.devtools.artifactregistry.v1.GetFileRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Base class for [ValueSource] implementations that provide a value by reading a file from Google Artifact Registry.
 *
 * Subclasses should implement the [doObtain] function, transforming an [InputStream] to an object of the desired type.
 *
 * **Configuration cache and sensitive files:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the file may contain (credentials, private keys, tokens) — will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched file may contain sensitive data, call the [ArtifactRegistryClientBuildService] client directly
 * inside a [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
 * Non-sensitive files (version manifests, metadata, changelogs) are safe to use at configuration time.
 */
abstract class AbstractArtifactValueSource<T : Any, P : AbstractArtifactValueSource.Parameters> : ValueSource<T, P> {
    interface Parameters : ValueSourceParameters {
        @get:Internal
        val service: Property<ArtifactRegistryClientBuildService>

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
                val response = parameters.service.get().getClient().getFile(request)
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
