package com.kelvsyc.gradle.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to GitHub Actions.
 *
 * These include [information on built-in environment variables](https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables),
 * and other information derived therein.
 */
abstract class GitHubActionsProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {
    /**
     * Provides `true` if Gradle is running in CI.
     */
    val ci = providers.environmentVariable("CI")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides the name of the action currently running, or the id of a step.
     *
     * The name `__run` is used if the current step does not have an id. The name may contain a number suffix if the
     * script or action is run more than once in a job.
     */
    val action = providers.environmentVariable("GITHUB_ACTION")

    val actionPath = providers.environmentVariable("GITHUB_ACTION_PATH")

    val actionRepository = providers.environmentVariable("GITHUB_ACTION_REPOSITORY")

    /**
     * Provides `true` when GitHub Actions is running a workflow.
     */
    val actions = providers.environmentVariable("GITHUB_ACTIONS")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    val actor = providers.environmentVariable("GITHUB_ACTOR")

    val actorId = providers.environmentVariable("GITHUB_ACTOR_ID")

    val apiUrl = providers.environmentVariable("GITHUB_API_URL")

    val baseRef = providers.environmentVariable("GITHUB_BASE_REF")

    /**
     * Provides the path on the runner to the file that sets variables from workflow commands.
     */
    val envPath = providers.environmentVariable("GITHUB_ENV")

    /**
     * Provides the file on the runner setting variables from workflow commands.
     */
    val envFile = layout.file(envPath.map(::File))

    val eventName = providers.environmentVariable("GITHUB_EVENT_NAME")

    /**
     * Provides the path to the file on the runner containing the full event webhook payload.
     */
    val eventPath = providers.environmentVariable("GITHUB_EVENT_PATH")

    /**
     * Provides the file on the runner containing the full event webhook payload.
     */
    val eventFile = layout.file(eventPath.map(::File))

    val graphqlUrl = providers.environmentVariable("GITHUB_GRAPHQL_URL")

    val headRef = providers.environmentVariable("GITHUB_HEAD_REF")

    val job = providers.environmentVariable("GITHUB_JOB")

    /**
     * Provides the path on the runner that sets the step's outputs from workflow commands.
     */
    val outputPath = providers.environmentVariable("GITHUB_OUTPUT")

    /**
     * Provides the file on the runner that sets the step's outputs from workflow commands
     */
    val outputFile = layout.file(outputPath.map(::File))

    /**
     * Provides the path on the runner that sets system `PATH` variables from workflow commands.
     */
    val pathPath = providers.environmentVariable("GITHUB_PATH")

    /**
     * Provides the file on the runner that sets system `PATH` variables from workflow commands.
     */
    val pathFile = layout.file(pathPath.map(::File))

    val ref = providers.environmentVariable("GITHUB_REF")

    val refName = providers.environmentVariable("GITHUB_REF_NAME")

    val refProtected = providers.environmentVariable("GITHUB_REF_PROTECTED")
        .map(String::toBooleanStrictOrNull)

    val refType = providers.environmentVariable("GITHUB_REF_TYPE")

    val repository = providers.environmentVariable("GITHUB_REPOSITORY")

    val repositoryId = providers.environmentVariable("GITHUB_REPOSITORY_ID")

    val repositoryOwner = providers.environmentVariable("GITHUB_REPOSITORY_OWNER")

    val repositoryOwnerId = providers.environmentVariable("GITHUB_REPOSITORY_OWNER_ID")

    val retentionDays = providers.environmentVariable("GITHUB_RETENTION_DAYS")
        .map(String::toIntOrNull)

    val runAttempt = providers.environmentVariable("GITHUB_RUN_ATTEMPT")
        .map(String::toIntOrNull)

    val runId = providers.environmentVariable("GITHUB_RUN_ID")
        .map(String::toIntOrNull)

    val runNumber = providers.environmentVariable("GITHUB_RUN_NUMBER")
        .map(String::toIntOrNull)

    val serverUrl = providers.environmentVariable("GITHUB_SERVER_URL")

    val sha = providers.environmentVariable("GITHUB_SHA")

    val stepSummaryPath = providers.environmentVariable("GITHUB_STEP_SUMMARY")

    val stepSummaryFile = layout.file(stepSummaryPath.map(::File))

    val triggeringActor = providers.environmentVariable("GITHUB_TRIGGERING_ACTOR")

    val workflow = providers.environmentVariable("GITHUB_WORKFLOW")

    val workflowRef = providers.environmentVariable("GITHUB_WORKFLOW_REF")

    val workflowSha = providers.environmentVariable("GITHUB_WORKFLOW_SHA")

    val workspacePath = providers.environmentVariable("GITHUB_WORKSPACE")

    val workspaceDirectory = layout.dir(workspacePath.map(::File))

    val runnerArch = providers.environmentVariable("RUNNER_ARCH")

    val runnerDebug = providers.environmentVariable("RUNNER_DEBUG").map { it == "1" }

    val runnerEnvironment = providers.environmentVariable("RUNNER_ENVIRONMENT")

    val runnerName = providers.environmentVariable("RUNNER_NAME")

    val runnerOs = providers.environmentVariable("RUNNER_OS")

    val runnerTempPath = providers.environmentVariable("RUNNER_TEMP")

    val runnerTempDirectory = layout.dir(runnerTempPath.map(::File))

    val runnerToolCachePath = providers.environmentVariable("RUNNER_TOOL_CACHE")

    val runnerToolCacheDirectory = layout.dir(runnerToolCachePath.map(::File))

    val workflowRunUrl = serverUrl
        .zip(repository) { url, repo -> "$url/$repo"}
        .zip(runId) { prefix, id -> "$prefix/actions/runs/$id" }
}
