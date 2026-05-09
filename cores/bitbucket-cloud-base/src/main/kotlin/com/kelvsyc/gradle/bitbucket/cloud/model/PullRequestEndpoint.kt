package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.JsonClass

/**
 * A pull request endpoint representing either the source or destination of a pull request.
 */
@JsonClass(generateAdapter = false)
data class PullRequestEndpoint(
    /**
     * The branch for this endpoint.
     */
    val branch: Branch? = null,

    /**
     * A summary of the repository for this endpoint.
     */
    val repository: RepositorySummary? = null,

    /**
     * The commit at the tip of this endpoint.
     */
    val commit: CommitSummary? = null,
)
