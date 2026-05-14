package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A comment on a Bitbucket Cloud pull request.
 */
@JsonClass(generateAdapter = false)
data class PullRequestComment(
    /**
     * The comment ID.
     */
    val id: Long? = null,

    /**
     * The comment content.
     */
    val content: CommentContent? = null,

    /**
     * The author of the comment.
     */
    val user: Account? = null,

    /**
     * The ISO 8601 timestamp when the comment was created.
     */
    @Json(name = "created_on")
    val createdOn: String? = null,

    /**
     * The ISO 8601 timestamp when the comment was last updated.
     */
    @Json(name = "updated_on")
    val updatedOn: String? = null,

    /**
     * The object type, typically `pullrequest_comment`.
     */
    val type: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}

/**
 * The content body of a pull request comment, available in multiple markup formats.
 */
@JsonClass(generateAdapter = false)
data class CommentContent(
    /**
     * The raw markup content (typically Markdown).
     */
    val raw: String? = null,

    /**
     * The rendered HTML content.
     */
    val html: String? = null,

    /**
     * The plain-text content with markup stripped.
     */
    val markup: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
