package com.kelvsyc.gradle.aws.java.ecr

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.ecr.model.BatchDeleteImageRequest
import software.amazon.awssdk.services.ecr.model.ImageIdentifier

/**
 * [WorkAction] implementation that deletes a set of images, by tag, from an ECR repository.
 *
 * The set of image tags is supplied via [Parameters.imageTags]. To delete by digest instead of tag, use the
 * underlying SDK directly.
 */
abstract class BatchDeleteImageAction : WorkAction<BatchDeleteImageAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The build service managing the ECR client. */
        @get:Internal
        val service: Property<EcrClientBuildService>

        /** The repository to delete images from. */
        val repositoryName: Property<String>

        /** Set of image tags to delete. */
        val imageTags: SetProperty<String>
    }

    override fun execute() {
        val ids = parameters.imageTags.get().map { tag ->
            ImageIdentifier.builder().imageTag(tag).build()
        }
        val request = BatchDeleteImageRequest.builder().apply {
            repositoryName(parameters.repositoryName.get())
            imageIds(ids)
        }.build()

        parameters.service.get().getClient().batchDeleteImage(request)
    }
}
