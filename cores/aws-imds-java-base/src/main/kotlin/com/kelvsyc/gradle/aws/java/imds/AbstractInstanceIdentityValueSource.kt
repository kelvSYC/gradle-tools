package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.core.document.Document
import software.amazon.awssdk.imds.Ec2MetadataClient

/**
 * Base class for [ValueSource]s providing values retrieved from the [IMDS Instance Identity Document](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html).
 *
 * Subclasses should implement the [doObtain] function, transforming a [Document] object to an object of the desired
 * type.
 */
abstract class AbstractInstanceIdentityValueSource<T, P : AbstractInstanceIdentityValueSource.Parameters> :
    ValueSource<T, P> {
    companion object {
        const val DOCUMENT_REQUEST_PATH = "/latest/dynamic/instance-identity/document"
    }

    /**
     * Base parameters interface for [AbstractInstanceIdentityValueSource]. This contains the data needed to retrieve
     * data from the IMDS instance identity document.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractInstanceIdentityValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>
    }

    private val client: Provider<Ec2MetadataClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the data retrieved from the IMDS Instance Identity document.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from the document, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(document: Document): T?

    override fun obtain(): T? {
        val response = client.get().get(DOCUMENT_REQUEST_PATH)
        return doObtain(response.asDocument())
    }
}
