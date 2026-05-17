package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A commit status in Gitea.
 */
@JsonClass(generateAdapter = false)
data class CommitStatus(
    /**
     * The unique identifier for the status.
     */
    val id: Long? = null,

    /**
     * The status state (e.g., `success`, `failure`, `error`, `pending`).
     */
    val status: String? = null,

    /**
     * The target URL for the status (typically a link to CI/CD results).
     */
    @param:Json(name = "target_url")
    val targetUrl: String? = null,

    /**
     * A description of the status.
     */
    val description: String? = null,

    /**
     * The context or name of the status check.
     */
    val context: String? = null,

    /**
     * The ISO 8601 timestamp when the status was created.
     */
    @param:Json(name = "created_at")
    val createdAt: String? = null,

    /**
     * The ISO 8601 timestamp when the status was last updated.
     */
    @param:Json(name = "updated_at")
    val updatedAt: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
