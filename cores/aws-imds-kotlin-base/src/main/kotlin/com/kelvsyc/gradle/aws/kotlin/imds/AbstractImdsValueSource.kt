package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.EC2MetadataError
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [EC2 Instance Metadata Service](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html).
 *
 * Subclasses should implement the [doObtain] function, transforming the raw IMDS response string to an object of the
 * desired type.
 *
 * **Configuration cache and sensitive IMDS paths:** Gradle serializes the result of every [ValueSource.obtain]
 * call to the configuration cache in plaintext when the cache is written. Most IMDS paths return non-sensitive
 * instance metadata (AMI ID, instance type, availability zone). However, some paths return sensitive data — for
 * example, `/latest/meta-data/iam/security-credentials/` returns temporary IAM credentials. Whatever [doObtain]
 * returns will be stored in `.gradle/configuration-cache/` and is readable by any process with access to the
 * build directory, regardless of whether the result is stored in a task `@Input`, `@get:Internal`, or private
 * `val` — all cause `obtain()` to run at configuration time and the result to be cached.
 *
 * If the queried IMDS path may return sensitive data, call the [ImdsClientBuildService] client directly inside a
 * [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
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
        @get:Internal
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
