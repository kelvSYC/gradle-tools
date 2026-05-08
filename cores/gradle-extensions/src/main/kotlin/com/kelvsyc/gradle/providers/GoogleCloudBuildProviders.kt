package com.kelvsyc.gradle.providers

import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to Google Cloud Build.
 *
 * Cloud Build exposes these as
 * [substitution variables](https://cloud.google.com/build/docs/configuring-builds/substitute-variable-values#using_default_substitutions)
 * in the build configuration. To make them available as environment variables in a build step, they must be explicitly
 * passed via the `env` field:
 *
 * ```yaml
 * steps:
 * - name: 'gradle'
 *   env:
 *   - 'PROJECT_ID=$PROJECT_ID'
 *   - 'BUILD_ID=$BUILD_ID'
 *   - 'COMMIT_SHA=$COMMIT_SHA'
 * ```
 *
 * The GitHub pull request variables (`_HEAD_BRANCH`, `_BASE_BRANCH`, `_HEAD_REPO_URL`, `_PR_NUMBER`) are only
 * populated for GitHub pull request triggers.
 */
abstract class GoogleCloudBuildProviders @Inject constructor(providers: ProviderFactory) {

    // region Always Available

    /**
     * Provides the ID of the Cloud project.
     */
    val projectId = providers.environmentVariable("PROJECT_ID")

    /**
     * Provides the ID of the build.
     */
    val buildId = providers.environmentVariable("BUILD_ID")

    /**
     * Provides the project number.
     */
    val projectNumber = providers.environmentVariable("PROJECT_NUMBER")

    /**
     * Provides the region associated with the build.
     */
    val location = providers.environmentVariable("LOCATION")

    // endregion

    // region Trigger-Invoked Builds

    /**
     * Provides the name associated with the build trigger. Only available for trigger-invoked builds.
     */
    val triggerName = providers.environmentVariable("TRIGGER_NAME")

    /**
     * Provides the full commit SHA associated with the build. Only available for trigger-invoked builds.
     */
    val commitSha = providers.environmentVariable("COMMIT_SHA")

    /**
     * Provides the commit ID associated with the build. Identical to [commitSha].
     */
    val revisionId = providers.environmentVariable("REVISION_ID")

    /**
     * Provides the first seven characters of the commit SHA. Only available for trigger-invoked builds.
     */
    val shortSha = providers.environmentVariable("SHORT_SHA")

    /**
     * Provides the name of the repository. Only available for trigger-invoked builds.
     */
    val repoName = providers.environmentVariable("REPO_NAME")

    /**
     * Provides the full name of the repository, including the user or organization. Only available for trigger-invoked
     * builds.
     */
    val repoFullName = providers.environmentVariable("REPO_FULL_NAME")

    /**
     * Provides the name of the branch. Only available for trigger-invoked builds.
     */
    val branchName = providers.environmentVariable("BRANCH_NAME")

    /**
     * Provides the name of the tag. Only available for trigger-invoked builds.
     */
    val tagName = providers.environmentVariable("TAG_NAME")

    /**
     * Provides the name of the branch or tag. Only available for trigger-invoked builds.
     */
    val refName = providers.environmentVariable("REF_NAME")

    /**
     * Provides the path to the build configuration file used during build execution, or an empty string if none was
     * used. Only available for trigger-invoked builds.
     */
    val triggerBuildConfigPath = providers.environmentVariable("TRIGGER_BUILD_CONFIG_PATH")

    /**
     * Provides the email of the service account used for the build. Only available for trigger-invoked builds.
     */
    val serviceAccountEmail = providers.environmentVariable("SERVICE_ACCOUNT_EMAIL")

    /**
     * Provides the resource name of the service account, in the format
     * `projects/PROJECT_ID/serviceAccounts/SERVICE_ACCOUNT_EMAIL`. Only available for trigger-invoked builds.
     */
    val serviceAccount = providers.environmentVariable("SERVICE_ACCOUNT")

    // endregion

    // region GitHub Pull Request Triggers

    /**
     * Provides the head branch of the pull request. Only available for GitHub pull request triggers.
     */
    val headBranch = providers.environmentVariable("_HEAD_BRANCH")

    /**
     * Provides the base branch of the pull request. Only available for GitHub pull request triggers.
     */
    val baseBranch = providers.environmentVariable("_BASE_BRANCH")

    /**
     * Provides the URL of the head repository of the pull request. Only available for GitHub pull request triggers.
     */
    val headRepoUrl = providers.environmentVariable("_HEAD_REPO_URL")

    /**
     * Provides the number of the pull request. Only available for GitHub pull request triggers.
     */
    val prNumber = providers.environmentVariable("_PR_NUMBER")
        .map(String::toIntOrNull)

    // endregion
}
