package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageRequest
import aws.sdk.kotlin.services.ecr.model.ImageIdentifier
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation that deletes a set of images, by tag, from an ECR repository.
 *
 * The set of image tags is supplied via [Parameters.imageTags]. To delete by digest instead of tag, use the
 * underlying SDK directly.
 */
abstract class BatchDeleteImageAction : WorkAction<BatchDeleteImageAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing ECR clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [EcrClientInfo]. */
        val clientName: Property<String>

        /** The repository to delete images from. */
        val repositoryName: Property<String>

        /** Set of image tags to delete. */
        val imageTags: SetProperty<String>
    }

    private val client: Provider<EcrClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = BatchDeleteImageRequest {
            repositoryName = parameters.repositoryName.get()
            imageIds = parameters.imageTags.get().map { tag ->
                ImageIdentifier { imageTag = tag }
            }
        }

        runBlocking {
            client.get().batchDeleteImage(request)
        }
    }
}
