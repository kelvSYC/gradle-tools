package com.kelvsyc.gradle.aws.java.imds

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.imds.Ec2MetadataResponse

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [EC2 Instance Metadata Service](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html).
 *
 * Subclasses should implement the [doObtain] function, transforming the [Ec2MetadataResponse] to an object of the
 * desired type.
 */
abstract class AbstractImdsValueSource<T : Any, P : AbstractImdsValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractImdsValueSource]. This contains the data needed to make an IMDS request.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractImdsValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing IMDS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [ImdsClientInfo]. */
        val clientName: Property<String>

        /** The IMDS metadata path to query. */
        val path: Property<String>
    }

    private val client: Provider<Ec2MetadataClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the IMDS response into the desired type.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The transformed data, or `null` if the response cannot be transformed.
     */
    abstract fun doObtain(response: Ec2MetadataResponse): T?

    override fun obtain(): T? {
        val response = client.get().get(parameters.path.get())
        return doObtain(response)
    }
}
