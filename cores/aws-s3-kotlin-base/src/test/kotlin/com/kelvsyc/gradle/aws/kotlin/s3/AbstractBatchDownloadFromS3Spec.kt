package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.mockk
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractBatchDownloadFromS3Spec : FunSpec() {
    init {
        test("Register single artifact - configures request parameters") {
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromS3>("myTask") {
                client.set(mockk<S3Client>())
                registerArtifact("artifact1") {
                    bucket.set("bucket")
                    key.set("key")
                    outputFile.set(project.layout.buildDirectory.file("filename"))
                }
            }

            val reqs = task.get().requests.get()
            reqs shouldHaveSize 1
            reqs[0].request.bucket!! shouldBeEqual "bucket"
            reqs[0].request.key!! shouldBeEqual "key"
        }

        test("Register multiple artifacts - configures all request parameters") {
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromS3>("myTask") {
                client.set(mockk<S3Client>())
                registerArtifact("artifact1") {
                    bucket.set("bucket1")
                    key.set("key1")
                    outputFile.set(project.layout.buildDirectory.file("filename1"))
                }
                registerArtifact("artifact2") {
                    bucket.set("bucket2")
                    key.set("key2")
                    outputFile.set(project.layout.buildDirectory.file("filename2"))
                }
            }

            val artifacts = task.get().artifacts.get()
            val reqs = task.get().requests.get()
            reqs.shouldHaveSize(artifacts.size)
            artifacts.values.forEach { artifact ->
                reqs.any { it.request.bucket == artifact.bucket.orNull && it.request.key == artifact.key.orNull }.shouldBeTrue()
            }
        }
    }
}
