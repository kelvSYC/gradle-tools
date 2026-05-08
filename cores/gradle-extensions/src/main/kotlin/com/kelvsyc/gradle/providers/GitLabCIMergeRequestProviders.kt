package com.kelvsyc.gradle.providers

import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * Analogue to [ProviderFactory], but for [Provider][org.gradle.api.provider.Provider]s relating to GitLab CI/CD merge
 * request pipelines.
 *
 * These variables are only available in merge request pipelines (when an open merge request exists for the source
 * branch). See [GitLab CI/CD Predefined Variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html).
 */
abstract class GitLabCIMergeRequestProviders @Inject constructor(providers: ProviderFactory) {
    /**
     * Provides whether the merge request has been approved.
     */
    val approved = providers.environmentVariable("CI_MERGE_REQUEST_APPROVED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides a comma-separated list of assignee usernames. Only set if the merge request has at least one assignee.
     */
    val assignees = providers.environmentVariable("CI_MERGE_REQUEST_ASSIGNEES")

    /**
     * Provides the base SHA of the merge request diff.
     */
    val diffBaseSha = providers.environmentVariable("CI_MERGE_REQUEST_DIFF_BASE_SHA")

    /**
     * Provides the version of the merge request diff.
     */
    val diffId = providers.environmentVariable("CI_MERGE_REQUEST_DIFF_ID")
        .map(String::toLongOrNull)

    /**
     * Provides whether the merge request is a draft.
     */
    val draft = providers.environmentVariable("CI_MERGE_REQUEST_DRAFT")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the merge request event type (`detached`, `merged_result`, or `merge_train`).
     */
    val eventType = providers.environmentVariable("CI_MERGE_REQUEST_EVENT_TYPE")

    /**
     * Provides the description of the merge request (first 2700 characters).
     */
    val description = providers.environmentVariable("CI_MERGE_REQUEST_DESCRIPTION")

    /**
     * Provides whether the merge request description was truncated (exceeds 2700 characters).
     */
    val descriptionIsTruncated = providers.environmentVariable("CI_MERGE_REQUEST_DESCRIPTION_IS_TRUNCATED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the instance-level ID of the merge request, unique across all projects.
     */
    val id = providers.environmentVariable("CI_MERGE_REQUEST_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the project-level IID of the merge request (the number shown in the URL).
     */
    val iid = providers.environmentVariable("CI_MERGE_REQUEST_IID")
        .map(String::toIntOrNull)

    /**
     * Provides a comma-separated list of label names. Only set if the merge request has at least one label.
     */
    val labels = providers.environmentVariable("CI_MERGE_REQUEST_LABELS")

    /**
     * Provides the milestone title. Only set if the merge request has a milestone.
     */
    val milestone = providers.environmentVariable("CI_MERGE_REQUEST_MILESTONE")

    /**
     * Provides the ID of the merge request's project.
     */
    val projectId = providers.environmentVariable("CI_MERGE_REQUEST_PROJECT_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the path of the merge request's project (e.g. `namespace/awesome-project`).
     */
    val projectPath = providers.environmentVariable("CI_MERGE_REQUEST_PROJECT_PATH")

    /**
     * Provides the URL of the merge request's project.
     */
    val projectUrl = providers.environmentVariable("CI_MERGE_REQUEST_PROJECT_URL")

    /**
     * Provides the ref path of the merge request (e.g. `refs/merge-requests/1/head`).
     */
    val refPath = providers.environmentVariable("CI_MERGE_REQUEST_REF_PATH")

    /**
     * Provides the source branch name.
     */
    val sourceBranchName = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")

    /**
     * Provides whether the source branch is protected.
     */
    val sourceBranchProtected = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_BRANCH_PROTECTED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the HEAD SHA of the source branch. Only present in merged results pipelines.
     */
    val sourceBranchSha = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_BRANCH_SHA")

    /**
     * Provides the ID of the source project.
     */
    val sourceProjectId = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_PROJECT_ID")
        .map(String::toLongOrNull)

    /**
     * Provides the path of the source project.
     */
    val sourceProjectPath = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_PROJECT_PATH")

    /**
     * Provides the URL of the source project.
     */
    val sourceProjectUrl = providers.environmentVariable("CI_MERGE_REQUEST_SOURCE_PROJECT_URL")

    /**
     * Provides whether "squash on merge" is enabled for this merge request.
     */
    val squashOnMerge = providers.environmentVariable("CI_MERGE_REQUEST_SQUASH_ON_MERGE")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the target branch name.
     */
    val targetBranchName = providers.environmentVariable("CI_MERGE_REQUEST_TARGET_BRANCH_NAME")

    /**
     * Provides whether the target branch is protected.
     */
    val targetBranchProtected = providers.environmentVariable("CI_MERGE_REQUEST_TARGET_BRANCH_PROTECTED")
        .map(String::toBooleanStrictOrNull)

    /**
     * Provides the HEAD SHA of the target branch. Only present in merged results pipelines.
     */
    val targetBranchSha = providers.environmentVariable("CI_MERGE_REQUEST_TARGET_BRANCH_SHA")

    /**
     * Provides the title of the merge request.
     */
    val title = providers.environmentVariable("CI_MERGE_REQUEST_TITLE")
}
