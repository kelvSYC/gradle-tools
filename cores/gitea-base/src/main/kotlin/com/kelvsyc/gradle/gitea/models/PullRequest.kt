package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Gitea pull request.
 */
@JsonClass(generateAdapter = false)
data class PullRequest(
    /**
     * The unique identifier for the pull request.
     */
    val id: Long? = null,

    /**
     * The pull request number.
     */
    val number: Long? = null,

    /**
     * The pull request title.
     */
    val title: String? = null,

    /**
     * The pull request description body.
     */
    val body: String? = null,

    /**
     * The state of the pull request (e.g., `open`, `closed`).
     */
    val state: String? = null,

    /**
     * The head (source) branch reference.
     */
    val head: PullRequestRef? = null,

    /**
     * The base (target) branch reference.
     */
    val base: PullRequestRef? = null,

    /**
     * The user who created the pull request.
     */
    val user: User? = null,

    /**
     * The ISO 8601 timestamp when the pull request was merged, or null if not merged.
     */
    @Json(name = "merged_at")
    val mergedAt: String? = null,

    /**
     * The ISO 8601 timestamp when the pull request was created.
     */
    @Json(name = "created_at")
    val createdAt: String? = null,

    /**
     * The ISO 8601 timestamp when the pull request was last updated.
     */
    @Json(name = "updated_at")
    val updatedAt: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
