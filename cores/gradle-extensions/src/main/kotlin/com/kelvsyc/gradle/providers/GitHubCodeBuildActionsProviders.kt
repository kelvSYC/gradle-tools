package com.kelvsyc.gradle.providers

import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to using GitHub Actions
 * runners hosted by AWS CodeBuild.
 *
 * See [AWS CodeBuild Documentation](https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html)
 */
abstract class GitHubCodeBuildActionsProviders @Inject constructor(providers: ProviderFactory) {
    val runnerOwner = providers.environmentVariable("CODEBUILD_RUNNER_OWNER")

    val runnerRepo = providers.environmentVariable("CODEBUILD_RUNNER_REPO")

    val runnerRepoDomain = providers.environmentVariable("CODEBUILD_RUNNER_REPO_DOMAIN")

    val webhookLabel = providers.environmentVariable("CODEBUILD_WEBHOOK_LABEL")

    val webhookRunId = providers.environmentVariable("CODEBUILD_WEBHOOK_RUN_ID")

    val webhookJobId = providers.environmentVariable("CODEBUILD_WEBHOOK_JOB_ID")

    val webhookWorkflowName = providers.environmentVariable("CODEBUILD_WEBHOOK_WORKFLOW_NAME")

    val runnerWithBuildspec = providers.environmentVariable("CODEBUILD_RUNNER_WITH_BUILDSPEC")
        .mapKt { it.toBooleanStrictOrNull() }
        .orElse(false)
}
