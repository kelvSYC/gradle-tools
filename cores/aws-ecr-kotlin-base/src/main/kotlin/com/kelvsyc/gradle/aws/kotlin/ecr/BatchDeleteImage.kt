package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.BatchDeleteImageRequest
import aws.sdk.kotlin.services.ecr.model.ImageIdentifier
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * [DefaultTask] implementation that deletes a set of images, by tag, from an ECR repository.
 *
 * The set of image tags is supplied via [imageTags]. To delete by digest instead of tag, use the
 * underlying SDK directly.
 */
@UntrackedTask(because = "Communicates with AWS ECR; no local output")
abstract class BatchDeleteImage : DefaultTask() {

    /**
     * The build service managing the ECR client.
     * Excluded from task snapshots.
     */
    @get:Internal
    abstract val service: Property<EcrClientBuildService>

    /**
     * The ECR repository to delete images from.
     */
    @get:Input
    abstract val repositoryName: Property<String>

    /**
     * Set of image tags to delete.
     */
    @get:Input
    abstract val imageTags: SetProperty<String>

    /**
     * Submits the batch delete request to ECR.
     *
     * Constructs a [BatchDeleteImageRequest] from [repositoryName] and [imageTags], then invokes
     * the ECR client to perform the deletion.
     */
    @TaskAction
    fun execute() {
        val request = BatchDeleteImageRequest {
            repositoryName = this@BatchDeleteImage.repositoryName.get()
            imageIds = this@BatchDeleteImage.imageTags.get().map { tag ->
                ImageIdentifier { imageTag = tag }
            }
        }

        runBlocking {
            service.get().getClient().batchDeleteImage(request)
        }
    }
}
