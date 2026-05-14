package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A comment on a pull request in Bitbucket Data Center.
 */
@JsonClass(generateAdapter = false)
data class Comment(
    /**
     * The comment ID.
     */
    val id: Long? = null,

    /**
     * The comment text.
     */
    val text: String? = null,

    /**
     * The author of the comment.
     */
    val author: User? = null,

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

    /**
     * The comment version, used for optimistic locking on updates.
     */
    val version: Int? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
