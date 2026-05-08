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
     * Provides `true` if Gradle is running in CI. Defaults to `false` when the `CI` environment variable is absent.
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

    /**
     * Provides the path where an action is located. Only supported in composite actions.
     */
    val actionPath = providers.environmentVariable("GITHUB_ACTION_PATH")

    /**
     * Provides the owner and repository name of the action being executed (e.g. `actions/checkout`).
     */
    val actionRepository = providers.environmentVariable("GITHUB_ACTION_REPOSITORY")

    /**
     * Provides `true` when GitHub Actions is running a workflow. Defaults to `false` when the `GITHUB_ACTIONS`
     * environment variable is absent.
     */
    val actions = providers.environmentVariable("GITHUB_ACTIONS")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides the name of the person or app that initiated the workflow (e.g. `octocat`).
     */
    val actor = providers.environmentVariable("GITHUB_ACTOR")

    /**
     * Provides the account ID of the person or app that triggered the initial workflow run.
     */
    val actorId = providers.environmentVariable("GITHUB_ACTOR_ID")

    /**
     * Provides the GitHub API URL (e.g. `https://api.github.com`).
     */
    val apiUrl = providers.environmentVariable("GITHUB_API_URL")

    /**
     * Provides the target branch of the pull request. Only set for `pull_request` or `pull_request_target` events.
     */
    val baseRef = providers.environmentVariable("GITHUB_BASE_REF")

    /**
     * Provides the path on the runner to the file that sets variables from workflow commands.
     */
    val envPath = providers.environmentVariable("GITHUB_ENV")

    /**
     * Provides the file on the runner setting variables from workflow commands.
     */
    val envFile = layout.file(envPath.map(::File))

    /**
     * Provides the name of the event that triggered the workflow (e.g. `workflow_dispatch`).
     */
    val eventName = providers.environmentVariable("GITHUB_EVENT_NAME")

    /**
     * Provides the path to the file on the runner containing the full event webhook payload.
     */
    val eventPath = providers.environmentVariable("GITHUB_EVENT_PATH")

    /**
     * Provides the file on the runner containing the full event webhook payload.
     */
    val eventFile = layout.file(eventPath.map(::File))

    /**
     * Provides the GitHub GraphQL API URL (e.g. `https://api.github.com/graphql`).
     */
    val graphqlUrl = providers.environmentVariable("GITHUB_GRAPHQL_URL")

    /**
     * Provides the source branch of the pull request. Only set for `pull_request` or `pull_request_target` events.
     */
    val headRef = providers.environmentVariable("GITHUB_HEAD_REF")

    /**
     * Provides the `job_id` of the current job.
     */
    val job = providers.environmentVariable("GITHUB_JOB")

    /**
     * Provides the path on the runner that sets the step's outputs from workflow commands.
     */
    val outputPath = providers.environmentVariable("GITHUB_OUTPUT")

    /**
     * Provides the file on the runner that sets the step's outputs from workflow commands.
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

    /**
     * Provides the fully-formed ref of the branch or tag that triggered the workflow run (e.g.
     * `refs/heads/feature-branch`).
     */
    val ref = providers.environmentVariable("GITHUB_REF")

    /**
     * Provides the short ref name of the branch or tag that triggered the workflow run (e.g. `feature-branch`).
     */
    val refName = providers.environmentVariable("GITHUB_REF_NAME")

    /**
     * Provides `true` if branch protections or rulesets are configured for the ref that triggered the workflow run.
     * Absent when the environment variable is unset.
     */
    val refProtected = providers.environmentVariable("GITHUB_REF_PROTECTED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the type of ref that triggered the workflow run (`branch` or `tag`).
     */
    val refType = providers.environmentVariable("GITHUB_REF_TYPE")

    /**
     * Provides the owner and repository name (e.g. `octocat/Hello-World`).
     */
    val repository = providers.environmentVariable("GITHUB_REPOSITORY")

    /**
     * Provides the numeric ID of the repository.
     */
    val repositoryId = providers.environmentVariable("GITHUB_REPOSITORY_ID")

    /**
     * Provides the repository owner's name (e.g. `octocat`).
     */
    val repositoryOwner = providers.environmentVariable("GITHUB_REPOSITORY_OWNER")

    /**
     * Provides the repository owner's account ID.
     */
    val repositoryOwnerId = providers.environmentVariable("GITHUB_REPOSITORY_OWNER_ID")

    /**
     * Provides the number of days that workflow run logs and artifacts are kept.
     */
    val retentionDays = providers.environmentVariable("GITHUB_RETENTION_DAYS")
        .map(String::toIntOrNull)

    /**
     * Provides the attempt number of the current workflow run, starting at 1 and incrementing with each re-run.
     */
    val runAttempt = providers.environmentVariable("GITHUB_RUN_ATTEMPT")
        .map(String::toIntOrNull)

    /**
     * Provides the unique numeric ID of the workflow run. Does not change on re-run.
     */
    val runId = providers.environmentVariable("GITHUB_RUN_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the unique run number for this workflow, starting at 1 and incrementing with each new run. Does not
     * change on re-run.
     */
    val runNumber = providers.environmentVariable("GITHUB_RUN_NUMBER")
        .map(String::toIntOrNull)

    /**
     * Provides the URL of the GitHub server (e.g. `https://github.com`).
     */
    val serverUrl = providers.environmentVariable("GITHUB_SERVER_URL")

    /**
     * Provides the commit SHA that triggered the workflow.
     */
    val sha = providers.environmentVariable("GITHUB_SHA")

    /**
     * Provides the path on the runner to the file that contains job summaries from workflow commands.
     */
    val stepSummaryPath = providers.environmentVariable("GITHUB_STEP_SUMMARY")

    /**
     * Provides the file on the runner that contains job summaries from workflow commands.
     */
    val stepSummaryFile = layout.file(stepSummaryPath.map(::File))

    /**
     * Provides the username of the user that initiated the workflow run. May differ from [actor] on re-runs.
     */
    val triggeringActor = providers.environmentVariable("GITHUB_TRIGGERING_ACTOR")

    /**
     * Provides the name of the workflow. If no `name` is set, the value is the workflow file's path in the repository.
     */
    val workflow = providers.environmentVariable("GITHUB_WORKFLOW")

    /**
     * Provides the ref path to the workflow (e.g.
     * `octocat/hello-world/.github/workflows/my-workflow.yml@refs/heads/my_branch`).
     */
    val workflowRef = providers.environmentVariable("GITHUB_WORKFLOW_REF")

    /**
     * Provides the commit SHA for the workflow file.
     */
    val workflowSha = providers.environmentVariable("GITHUB_WORKFLOW_SHA")

    /**
     * Provides the default working directory path on the runner for steps.
     */
    val workspacePath = providers.environmentVariable("GITHUB_WORKSPACE")

    /**
     * Provides the default working directory on the runner for steps, as a [org.gradle.api.file.Directory].
     */
    val workspaceDirectory = layout.dir(workspacePath.map(::File))

    /**
     * Provides the architecture of the runner (`X86`, `X64`, `ARM`, or `ARM64`).
     */
    val runnerArch = providers.environmentVariable("RUNNER_ARCH")

    /**
     * Provides `true` if debug logging is enabled on the runner. Absent when debug logging is not enabled.
     */
    val runnerDebug = providers.environmentVariable("RUNNER_DEBUG").map { it == "1" }

    /**
     * Provides the environment of the runner (`github-hosted` or `self-hosted`).
     */
    val runnerEnvironment = providers.environmentVariable("RUNNER_ENVIRONMENT")

    /**
     * Provides the name of the runner executing the job.
     */
    val runnerName = providers.environmentVariable("RUNNER_NAME")

    /**
     * Provides the operating system of the runner (`Linux`, `Windows`, or `macOS`).
     */
    val runnerOs = providers.environmentVariable("RUNNER_OS")

    /**
     * Provides the path to a temporary directory on the runner, emptied at the beginning and end of each job.
     */
    val runnerTempPath = providers.environmentVariable("RUNNER_TEMP")

    /**
     * Provides the temporary directory on the runner, as a [org.gradle.api.file.Directory].
     */
    val runnerTempDirectory = layout.dir(runnerTempPath.map(::File))

    /**
     * Provides the path to the directory containing preinstalled tools for GitHub-hosted runners.
     */
    val runnerToolCachePath = providers.environmentVariable("RUNNER_TOOL_CACHE")

    /**
     * Provides the directory containing preinstalled tools for GitHub-hosted runners, as a
     * [org.gradle.api.file.Directory].
     */
    val runnerToolCacheDirectory = layout.dir(runnerToolCachePath.map(::File))

    /**
     * Provides the URL of the workflow run, derived from [serverUrl], [repository], and [runId].
     */
    val workflowRunUrl = serverUrl
        .zip(repository) { url, repo -> "$url/$repo" }
        .zip(runId) { prefix, id -> "$prefix/actions/runs/$id" }
}
