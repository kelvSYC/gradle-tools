package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageRequest
import aws.sdk.kotlin.services.ecr.model.ImageIdentifier
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes a set of images, by tag, from an ECR repository.
 *
 * The set of image tags is supplied via [Parameters.imageTags]. To delete by digest instead of tag, use the
 * underlying SDK directly.
 */
abstract class BatchDeleteImageAction : WorkAction<BatchDeleteImageAction.Parameters> {
    /**
     * Parameters for [BatchDeleteImageAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the ECR client. */
        val service: Property<EcrClientBuildService>

        /** The repository to delete images from. */
        val repositoryName: Property<String>

        /** Set of image tags to delete. */
        val imageTags: SetProperty<String>
    }

    override fun execute() {
        val request = BatchDeleteImageRequest {
            repositoryName = parameters.repositoryName.get()
            imageIds = parameters.imageTags.get().map { tag ->
                ImageIdentifier { imageTag = tag }
            }
        }

        runBlocking {
            parameters.service.get().getClient().batchDeleteImage(request)
        }
    }
}
