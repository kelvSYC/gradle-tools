package com.kelvsyc.gradle.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to CircleCI.
 *
 * See [CircleCI Built-in Environment Variables](https://circleci.com/docs/variables/#built-in-environment-variables).
 * Sensitive variables (OIDC tokens) and internal variables are intentionally excluded.
 */
abstract class CircleCIProviders @Inject constructor(layout: ProjectLayout, providers: ProviderFactory) {

    /**
     * Provides `true` if Gradle is running in a CI environment. Defaults to `false` when the `CI` environment variable
     * is absent.
     */
    val ci = providers.environmentVariable("CI")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides `true` if the current environment is CircleCI. Defaults to `false` when the `CIRCLECI` environment
     * variable is absent.
     */
    val circleCI = providers.environmentVariable("CIRCLECI")
        .map(String::toBooleanStrictOrNull)
        .orElse(false)

    /**
     * Provides the name of the Git branch currently being built.
     */
    val branch = providers.environmentVariable("CIRCLE_BRANCH")

    /**
     * Provides the number of the current job, unique for each job.
     */
    val buildNum = providers.environmentVariable("CIRCLE_BUILD_NUM")
        .map(String::toIntOrNull)

    /**
     * Provides the URL for the current job on CircleCI.
     */
    val buildUrl = providers.environmentVariable("CIRCLE_BUILD_URL")

    /**
     * Provides the name of the current job.
     */
    val job = providers.environmentVariable("CIRCLE_JOB")

    /**
     * Provides the index of the current parallel run instance, ranging from 0 to (`nodeTotal` - 1).
     */
    val nodeIndex = providers.environmentVariable("CIRCLE_NODE_INDEX")
        .map(String::toIntOrNull)

    /**
     * Provides the total number of parallel runs, as configured by the `parallelism` key.
     */
    val nodeTotal = providers.environmentVariable("CIRCLE_NODE_TOTAL")
        .map(String::toIntOrNull)

    /**
     * Provides a unique identifier for the CircleCI organization.
     */
    val organizationId = providers.environmentVariable("CIRCLE_ORGANIZATION_ID")

    /**
     * Provides a unique identifier for the current pipeline.
     */
    val pipelineId = providers.environmentVariable("CIRCLE_PIPELINE_ID")

    /**
     * Provides the number of the associated pull request. Only available on forked PRs (GitHub OAuth and Bitbucket
     * Cloud only).
     */
    val prNumber = providers.environmentVariable("CIRCLE_PR_NUMBER")
        .map(String::toIntOrNull)

    /**
     * Provides the name of the repository where the pull request was created. Only available on forked PRs (GitHub
     * OAuth and Bitbucket Cloud only).
     */
    val prRepoName = providers.environmentVariable("CIRCLE_PR_REPONAME")

    /**
     * Provides the username of the user who created the pull request. Only available on forked PRs (GitHub OAuth and
     * Bitbucket Cloud only).
     */
    val prUsername = providers.environmentVariable("CIRCLE_PR_USERNAME")

    /**
     * Provides the largest job number on the current branch that is less than the current job number. Not always set
     * and not deterministic.
     */
    val previousBuildNum = providers.environmentVariable("CIRCLE_PREVIOUS_BUILD_NUM")
        .map(String::toIntOrNull)

    /**
     * Provides a unique identifier for the current project.
     */
    val projectId = providers.environmentVariable("CIRCLE_PROJECT_ID")

    /**
     * Provides the name of the repository of the current project.
     */
    val projectRepoName = providers.environmentVariable("CIRCLE_PROJECT_REPONAME")

    /**
     * Provides the GitHub or Bitbucket username of the current project.
     */
    val projectUsername = providers.environmentVariable("CIRCLE_PROJECT_USERNAME")

    /**
     * Provides the URL of the associated pull request. If there are multiple, one URL is chosen at random.
     */
    val pullRequest = providers.environmentVariable("CIRCLE_PULL_REQUEST")

    /**
     * Provides a comma-separated list of URLs of the current build's associated pull requests. GitHub OAuth and
     * Bitbucket Cloud only.
     */
    val pullRequests = providers.environmentVariable("CIRCLE_PULL_REQUESTS")

    /**
     * Provides the URL of the GitHub or Bitbucket repository. GitHub OAuth and Bitbucket Cloud only.
     */
    val repositoryUrl = providers.environmentVariable("CIRCLE_REPOSITORY_URL")

    /**
     * Provides the SHA1 hash of the last commit of the current build.
     */
    val sha1 = providers.environmentVariable("CIRCLE_SHA1")

    /**
     * Provides the name of the git tag, if the current build is tagged.
     */
    val tag = providers.environmentVariable("CIRCLE_TAG")

    /**
     * Provides the GitHub or Bitbucket username of the user who triggered the pipeline.
     */
    val username = providers.environmentVariable("CIRCLE_USERNAME")

    /**
     * Provides a unique identifier for the workflow instance of the current job. Same for every job in a given
     * workflow instance.
     */
    val workflowId = providers.environmentVariable("CIRCLE_WORKFLOW_ID")

    /**
     * Provides a unique identifier for the current job within the workflow context.
     */
    val workflowJobId = providers.environmentVariable("CIRCLE_WORKFLOW_JOB_ID")

    /**
     * Provides an identifier for the workspace of the current job. Same for every job in a given workflow.
     */
    val workflowWorkspaceId = providers.environmentVariable("CIRCLE_WORKFLOW_WORKSPACE_ID")

    /**
     * Provides the path of the working directory for the current job.
     */
    val workingDirectoryPath = providers.environmentVariable("CIRCLE_WORKING_DIRECTORY")

    /**
     * Provides the working directory for the current job, as a [org.gradle.api.file.Directory].
     */
    val workingDirectory = layout.dir(workingDirectoryPath.map(::File))
}
