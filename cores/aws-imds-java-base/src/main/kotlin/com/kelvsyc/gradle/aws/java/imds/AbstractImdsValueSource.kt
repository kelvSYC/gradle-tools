package com.kelvsyc.gradle.aws.java.imds

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.imds.Ec2MetadataResponse

/**
 * Base class for [ValueSource]s providing values retrieved from the
 * [EC2 Instance Metadata Service](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html).
 *
 * Subclasses should implement the [doObtain] function, transforming the [Ec2MetadataResponse] to an object of the
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
     * Base parameters interface for [AbstractImdsValueSource]. This contains the data needed to make an IMDS request.
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
     * Transforms the IMDS response into the desired type.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The transformed data, or `null` if the response cannot be transformed.
     */
    abstract fun doObtain(response: Ec2MetadataResponse): T?

    override fun obtain(): T? {
        val response = parameters.service.get().getClient().get(parameters.path.get())
        return doObtain(response)
    }
}
