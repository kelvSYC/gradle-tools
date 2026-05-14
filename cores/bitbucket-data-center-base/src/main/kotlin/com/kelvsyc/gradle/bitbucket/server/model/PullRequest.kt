package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Bitbucket Data Center pull request.
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
     * The pull request state: `OPEN`, `MERGED`, or `DECLINED`.
     */
    val state: String? = null,

    /**
     * The source ref (branch the PR is from).
     */
    @Json(name = "fromRef")
    val fromRef: PullRequestRef? = null,

    /**
     * The destination ref (branch the PR targets).
     */
    @Json(name = "toRef")
    val toRef: PullRequestRef? = null,

    /**
     * The pull request author.
     */
    val author: PullRequestParticipant? = null,

    /**
     * The list of reviewers.
     */
    val reviewers: List<PullRequestParticipant>? = null,

    /**
     * The list of all participants (author, reviewers, observers).
     */
    val participants: List<PullRequestParticipant>? = null,

    /**
     * Whether the pull request is open.
     */
    val open: Boolean? = null,

    /**
     * Whether the pull request has been closed (merged or declined).
     */
    val closed: Boolean? = null,

    /**
     * The creation timestamp in milliseconds since epoch.
     */
    @Json(name = "createdDate")
    val createdDate: Long? = null,

    /**
     * The last-updated timestamp in milliseconds since epoch.
     */
    @Json(name = "updatedDate")
    val updatedDate: Long? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
