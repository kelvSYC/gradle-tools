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
    /**
     * Provides the owner of the repository that triggers the self-hosted runner build.
     */
    val runnerOwner = providers.environmentVariable("CODEBUILD_RUNNER_OWNER")

    /**
     * Provides the name of the repository that triggers the self-hosted runner build.
     */
    val runnerRepo = providers.environmentVariable("CODEBUILD_RUNNER_REPO")

    /**
     * Provides the domain of the repository. Only set for GitHub Enterprise builds.
     */
    val runnerRepoDomain = providers.environmentVariable("CODEBUILD_RUNNER_REPO_DOMAIN")

    /**
     * Provides the label used to configure build overrides and the self-hosted runner.
     */
    val webhookLabel = providers.environmentVariable("CODEBUILD_WEBHOOK_LABEL")

    /**
     * Provides the run ID of the workflow associated with the build.
     */
    val webhookRunId = providers.environmentVariable("CODEBUILD_WEBHOOK_RUN_ID")

    /**
     * Provides the job ID of the job associated with the build.
     */
    val webhookJobId = providers.environmentVariable("CODEBUILD_WEBHOOK_JOB_ID")

    /**
     * Provides the name of the workflow associated with the build, if present in the webhook request payload.
     */
    val webhookWorkflowName = providers.environmentVariable("CODEBUILD_WEBHOOK_WORKFLOW_NAME")

    /**
     * Provides whether a buildspec override is configured in the self-hosted runner request labels. Defaults to `false`
     * when the environment variable is absent.
     */
    val runnerWithBuildspec = providers.environmentVariable("CODEBUILD_RUNNER_WITH_BUILDSPEC")
        .map { it.toBooleanStrictOrNull() }
        .orElse(false)
}
