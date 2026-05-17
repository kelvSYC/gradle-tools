package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A comment in Gitea (on issues, pull requests, or commits).
 */
@JsonClass(generateAdapter = false)
data class Comment(
    /**
     * The unique identifier for the comment.
     */
    val id: Long? = null,

    /**
     * The comment body text.
     */
    val body: String? = null,

    /**
     * The user who created the comment.
     */
    val user: User? = null,

    /**
     * The ISO 8601 timestamp when the comment was created.
     */
    @Json(name = "created_at")
    val createdAt: String? = null,

    /**
     * The ISO 8601 timestamp when the comment was last updated.
     */
    @Json(name = "updated_at")
    val updatedAt: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
