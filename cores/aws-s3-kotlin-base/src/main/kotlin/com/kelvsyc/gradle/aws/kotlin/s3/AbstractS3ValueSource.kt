package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

abstract class AbstractS3ValueSource<T : Any, P : AbstractS3ValueSource.Parameters> : ValueSource<T, P> {
    interface Parameters : ValueSourceParameters {
        @get:Internal
        val service: Property<S3ClientBuildService>

        val bucket: Property<String>
        val key: Property<String>
    }

    abstract fun doObtain(response: GetObjectResponse): T?

    override fun obtain(): T? {
        val request = GetObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()
        }

        return runBlocking {
            parameters.service.get().getClient().getObject(request, ::doObtain)
        }
    }
}
