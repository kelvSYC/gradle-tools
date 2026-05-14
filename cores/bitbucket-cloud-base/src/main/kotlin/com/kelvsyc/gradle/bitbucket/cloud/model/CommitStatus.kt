package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A build status attached to a commit in Bitbucket Cloud.
 */
@JsonClass(generateAdapter = false)
data class CommitStatus(
    /**
     * The build state: `SUCCESSFUL`, `FAILED`, `INPROGRESS`, or `STOPPED`.
     */
    val state: String? = null,

    /**
     * A unique key identifying this build status.
     */
    val key: String? = null,

    /**
     * A short human-readable name for the build.
     */
    val name: String? = null,

    /**
     * A URL linking to the build results.
     */
    val url: String? = null,

    /**
     * A description of the build status.
     */
    val description: String? = null,

    /**
     * The ISO 8601 timestamp when the status was created.
     */
    @Json(name = "created_on")
    val createdOn: String? = null,

    /**
     * The ISO 8601 timestamp when the status was last updated.
     */
    @Json(name = "updated_on")
    val updatedOn: String? = null,

    /**
     * The object type, typically `build`.
     */
    val type: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
