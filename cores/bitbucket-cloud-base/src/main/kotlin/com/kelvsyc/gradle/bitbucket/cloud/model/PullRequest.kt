package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A Bitbucket Cloud pull request.
 */
@JsonClass(generateAdapter = false)
data class PullRequest(
    /**
     * The pull request ID (unique within the repository).
     */
    val id: Long? = null,

    /**
     * The pull request title.
     */
    val title: String? = null,

    /**
     * The pull request description.
     */
    val description: String? = null,

    /**
     * The pull request state: `OPEN`, `MERGED`, `DECLINED`, or `SUPERSEDED`.
     */
    val state: String? = null,

    /**
     * The source endpoint (branch and repository the PR is from).
     */
    val source: PullRequestEndpoint? = null,

    /**
     * The destination endpoint (branch and repository the PR targets).
     */
    val destination: PullRequestEndpoint? = null,

    /**
     * The author of the pull request.
     */
    val author: Account? = null,

    /**
     * The list of reviewers assigned to the pull request.
     */
    val reviewers: List<Account>? = null,

    /**
     * Whether the source branch should be closed after merge.
     */
    @Json(name = "close_source_branch")
    val closeSourceBranch: Boolean? = null,

    /**
     * The merge commit, present when the pull request has been merged.
     */
    @Json(name = "merge_commit")
    val mergeCommit: CommitSummary? = null,

    /**
     * The ISO 8601 timestamp when the pull request was created.
     */
    @Json(name = "created_on")
    val createdOn: String? = null,

    /**
     * The ISO 8601 timestamp when the pull request was last updated.
     */
    @Json(name = "updated_on")
    val updatedOn: String? = null,

    /**
     * The object type, typically `pullrequest`.
     */
    val type: String? = null,
)
