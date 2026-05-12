package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [EC2 Instance Metadata Service](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html).
 *
 * Subclasses should implement the [doObtain] function, transforming the raw IMDS response string to an object of the
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
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [ImdsClientInfo]. */
        val clientName: Property<String>

        /** The IMDS metadata path to query. */
        val path: Property<String>
    }

    private val client: Provider<ImdsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the raw IMDS response string into the desired type.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The transformed data, or `null` if the response cannot be transformed.
     */
    abstract fun doObtain(response: String): T?

    override fun obtain(): T? {
        return runBlocking {
            try {
                doObtain(client.get().get(parameters.path.get()))
            } catch (_: EC2MetadataError) {
                null
            }
        }
    }
}
