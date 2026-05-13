package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [EC2 Instance Metadata Service](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html).
 *
 * Subclasses should implement the [doObtain] function, transforming the raw IMDS response string to an object of the
 * desired type.
 */
abstract class AbstractImdsValueSource<T : Any, P : AbstractImdsValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractImdsValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractImdsValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        val service: Property<ImdsClientBuildService>

        /** The IMDS metadata path to query. */
        val path: Property<String>
    }

    /**
     * Transforms the raw IMDS response string into the desired type.
     *
     * @return The transformed data, or `null` if the response cannot be transformed.
     */
    abstract fun doObtain(response: String): T?

    override fun obtain(): T? {
        val client = parameters.service.get().getClient()
        return runBlocking {
            try {
                doObtain(client.get(parameters.path.get()))
            } catch (_: EC2MetadataError) {
                null
            }
        }
    }
}
