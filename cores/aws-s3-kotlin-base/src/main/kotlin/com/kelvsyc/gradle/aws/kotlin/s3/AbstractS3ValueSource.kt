package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

abstract class AbstractS3ValueSource<T : Any, P : AbstractS3ValueSource.Parameters> : ValueSource<T, P> {
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val bucket: Property<String>
        val key: Property<String>
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    abstract fun doObtain(response: GetObjectResponse): T?

    override fun obtain(): T? {
        val request = GetObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()
        }

        return runBlocking {
            client.get().getObject(request, ::doObtain)
        }
    }
}
