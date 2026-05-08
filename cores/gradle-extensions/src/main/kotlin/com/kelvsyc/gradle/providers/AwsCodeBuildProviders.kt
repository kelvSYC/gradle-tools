package com.kelvsyc.gradle.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to using AWS CodeBuild.
 *
 * See [AWS CodeBuild Documentation](https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html)
 */
abstract class AwsCodeBuildProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {
    /**
     * Provides the AWS Region where the build is running, as used by the AWS CLI.
     */
    val awsDefaultRegion = providers.environmentVariable("AWS_DEFAULT_REGION")

    /**
     * Provides the AWS Region where the build is running, as used by the AWS SDKs.
     */
    val awsRegion = providers.environmentVariable("AWS_REGION")

    /**
     * Provides the identifier of the build in a batch build, as specified in the batch buildspec.
     */
    val batchBuildIdentifier = providers.environmentVariable("CODEBUILD_BATCH_BUILD_IDENTIFIER")

    /**
     * Provides the Amazon Resource Name (ARN) of the build.
     */
    val buildArn = providers.environmentVariable("CODEBUILD_BUILD_ARN")

    /**
     * Provides the CodeBuild ID of the build.
     */
    val buildId = providers.environmentVariable("CODEBUILD_BUILD_ID")

    /**
     * Provides the CodeBuild build image identifier.
     */
    val buildImage = providers.environmentVariable("CODEBUILD_BUILD_IMAGE")

    /**
     * Provides the current build number for the project.
     */
    val buildNumber = providers.environmentVariable("CODEBUILD_BUILD_NUMBER")

    /**
     * Provides whether the current build is succeeding. Maps `"1"` to `true` and any other value to `false`.
     */
    val buildSucceeding = providers.environmentVariable("CODEBUILD_BUILD_SUCCEEDING")
        .map { it == "1" }

    /**
     * Provides the URL of the build results for this build.
     */
    val buildUrl = providers.environmentVariable("CODEBUILD_BUILD_URL")

    /**
     * Provides the entity that started the build (a pipeline name, username, or `CodeBuild-Jenkins-Plugin`).
     */
    val initiator = providers.environmentVariable("CODEBUILD_INITIATOR")

    /**
     * Provides the identifier of the AWS KMS key used to encrypt the build output artifact.
     */
    val kmsKeyId = providers.environmentVariable("CODEBUILD_KMS_KEY_ID")

    /**
     * Provides the Amazon Resource Name (ARN) of the build project.
     */
    val projectArn = providers.environmentVariable("CODEBUILD_PROJECT_ARN")

    /**
     * Provides the URL of the build results on the public builds website. Only set if public builds are enabled.
     */
    val publicBuildUrl = providers.environmentVariable("CODEBUILD_PUBLIC_BUILD_URL")

    /**
     * Provides the resolved version identifier of the build's source code (e.g. a commit ID). Available after the
     * `DOWNLOAD_SOURCE` phase.
     */
    val resolvedSourceVersion = providers.environmentVariable("CODEBUILD_RESOLVED_SOURCE_VERSION")

    /**
     * Provides the URL to the input artifact or source code repository.
     */
    val sourceRepoUrl = providers.environmentVariable("CODEBUILD_SOURCE_REPO_URL")

    /**
     * Provides the version ID, commit ID, branch name, or tag name associated with the primary source.
     */
    val sourceVersion = providers.environmentVariable("CODEBUILD_SOURCE_VERSION")

    /**
     * Provides a map of secondary source identifiers to their version strings. Keys have the
     * `CODEBUILD_SOURCE_VERSION_` prefix stripped.
     */
    val sourceVersions = providers.environmentVariablesPrefixedBy("CODEBUILD_SOURCE_VERSION_").map {
        it.mapKeys { it.key.removePrefix("CODEBUILD_SOURCE_VERSION_") }
    }

    /**
     * Provides the directory path that CodeBuild uses for the primary source.
     */
    val srcDirPath = providers.environmentVariable("CODEBUILD_SRC_DIR")

    /**
     * Provides the directory that CodeBuild uses for the primary source, as a [org.gradle.api.file.Directory].
     */
    val srcDir = layout.dir(srcDirPath.map(::File))

    /**
     * Provides a map of secondary source identifiers to their directory paths. Keys have the `CODEBUILD_SRC_DIR_`
     * prefix stripped.
     */
    val srcDirPaths = providers.environmentVariablesPrefixedBy("CODEBUILD_SRC_DIR_").map {
        it.mapKeys { it.key.removePrefix("CODEBUILD_SRC_DIR_") }
    }

    /**
     * Provides a map of secondary source identifiers to their directories, as [org.gradle.api.file.Directory]
     * instances. Keys have the `CODEBUILD_SRC_DIR_` prefix stripped.
     */
    val srcDirs = srcDirPaths.map {
        it.mapValues { layout.projectDirectory.dir(it.value) }
    }

    /**
     * Provides the start time of the build as a Unix timestamp in milliseconds.
     */
    val startTime = providers.environmentVariable("CODEBUILD_START_TIME")

    /**
     * Provides the account ID of the user that triggered the webhook event.
     */
    val webhookActorAccountId = providers.environmentVariable("CODEBUILD_WEBHOOK_ACTOR_ACCOUNT_ID")

    /**
     * Provides the base reference name of the webhook event (for pull requests, this is the target branch).
     */
    val webhookBaseRef = providers.environmentVariable("CODEBUILD_WEBHOOK_BASE_REF")

    /**
     * Provides the webhook event that triggers the current build.
     */
    val webhookEvent = providers.environmentVariable("CODEBUILD_WEBHOOK_EVENT")

    /**
     * Provides the head reference name of the webhook event (the source branch or tag).
     */
    val webhookHeadRef = providers.environmentVariable("CODEBUILD_WEBHOOK_HEAD_REF")

    /**
     * Provides the identifier of the merge commit, set when a Bitbucket pull request is merged with a squash strategy.
     */
    val webhookMergeCommit = providers.environmentVariable("CODEBUILD_WEBHOOK_MERGE_COMMIT")

    /**
     * Provides the ID of the most recent commit before the webhook push event.
     */
    val webhookPrevCommit = providers.environmentVariable("CODEBUILD_WEBHOOK_PREV_COMMIT")

    /**
     * Provides the webhook event that triggered the build (e.g. `pr/N`, `branch/name`, or `tag/name`).
     */
    val webhookTrigger = providers.environmentVariable("CODEBUILD_WEBHOOK_TRIGGER")
}
