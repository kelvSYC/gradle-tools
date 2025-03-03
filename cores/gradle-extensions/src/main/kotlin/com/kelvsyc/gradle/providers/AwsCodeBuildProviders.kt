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
    val awsDefaultRegion = providers.environmentVariable("AWS_DEFAULT_REGION")

    val awsRegion = providers.environmentVariable("AWS_REGION")

    val batchBuildIdentifier = providers.environmentVariable("CODEBUILD_BATCH_BUILD_IDENTIFIER")

    val buildArn = providers.environmentVariable("CODEBUILD_BUILD_ARN")

    val buildId = providers.environmentVariable("CODEBUILD_BUILD_ID")

    val buildImage = providers.environmentVariable("CODEBUILD_BUILD_IMAGE")

    val buildNumber = providers.environmentVariable("CODEBUILD_BUILD_NUMBER")

    val buildSucceeding = providers.environmentVariable("CODEBUILD_BUILD_SUCCEEDING")
        .map { it == "1"}

    val initiator = providers.environmentVariable("CODEBUILD_INITIATOR")

    val kmsKeyId = providers.environmentVariable("CODEBUILD_KMS_KEY_ID")

    val logPath = providers.environmentVariable("CODEBUILD_LOG_PATH")

    val publicBuildUrl = providers.environmentVariable("CODEBUILD_PUBLIC_BUILD_URL")

    val resolvedSourceVersion = providers.environmentVariable("CODEBUILD_RESOLVED_SOURCE_VERSION")

    val sourceRepoUrl = providers.environmentVariable("CODEBUILD_SOURCE_REPO_URL")

    val sourceVersion = providers.environmentVariable("CODEBUILD_SOURCE_VERSION")

    val sourceVersions = providers.environmentVariablesPrefixedBy("CODEBUILD_SOURCE_VERSION_").map {
        it.mapKeys { it.key.removePrefix("CODEBUILD_SOURCE_VERSION_") }
    }

    val srcDirPath = providers.environmentVariable("CODEBUILD_SRC_DIR")

    val srcDir = layout.dir(srcDirPath.map(::File))

    val srcDirPaths = providers.environmentVariablesPrefixedBy("CODEBUILD_SRC_DIR_").map {
        it.mapKeys { it.key.removePrefix("CODEBUILD_SRC_DIR_") }
    }

    val srcDirs = srcDirPaths.map {
        it.mapValues { layout.dir(providers.ofNullable(File(it.value))) }
    }

    val startTime = providers.environmentVariable("CODEBUILD_START_TIME")

    val webhookActorAccountId = providers.environmentVariable("CODEBUILD_WEBHOOK_ACTOR_ACCOUNT_ID")

    val webhookBaseRef = providers.environmentVariable("CODEBUILD_WEBHOOK_BASE_REF")

    val webhookEvent = providers.environmentVariable("CODEBUILD_WEBHOOK_EVENT")

    val webhookMergeCommit = providers.environmentVariable("CODEBUILD_WEBHOOK_MERGE_COMMIT")

    val webhookPrevCommit = providers.environmentVariable("CODEBUILD_WEBHOOK_PREV_COMMIT")

    val webhookHeadRef = providers.environmentVariable("CODEBUILD_WEBOOK_HEAD_REF")

    val webhookTrigger = providers.environmentVariable("CODEBUILD_WEBHOOK_TRIGGER")
}
