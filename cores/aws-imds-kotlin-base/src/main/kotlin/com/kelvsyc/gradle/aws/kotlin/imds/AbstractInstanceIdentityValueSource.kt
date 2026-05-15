package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [IMDS Instance Identity Document](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html).
 *
 * Subclasses should implement the [doObtain] function, transforming a string to an object of the desired type.
 */
abstract class AbstractInstanceIdentityValueSource<T : Any, P : AbstractInstanceIdentityValueSource.Parameters> :
    ValueSource<T, P> {
    companion object {
        const val DOCUMENT_REQUEST_PATH = "/latest/dynamic/instance-identity/document"
    }

    /**
     * Base parameters interface for [AbstractInstanceIdentityValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the
     * [AbstractInstanceIdentityValueSource] subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<ImdsClientBuildService>
    }

    /**
     * Transforms the data retrieved from the IMDS Instance Identity document.
     *
     * @return The data retrieved from the document, or `null` if the data cannot be transformed.
     */
    abstract fun doObtain(document: String): T?

    override fun obtain(): T? {
        val client = parameters.service.get().getClient()
        return runBlocking {
            try {
                doObtain(client.get(DOCUMENT_REQUEST_PATH))
            } catch (_: EC2MetadataError) {
                null
            }
        }
    }
}
