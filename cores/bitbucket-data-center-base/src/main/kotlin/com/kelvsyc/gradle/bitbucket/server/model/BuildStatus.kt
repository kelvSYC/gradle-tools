package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A build status attached to a commit in Bitbucket Data Center.
 */
@JsonClass(generateAdapter = false)
data class BuildStatus(
    /**
     * The build state: `SUCCESSFUL`, `FAILED`, or `INPROGRESS`.
     */
    val state: String? = null,

    /**
     * A unique key identifying this build.
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
     * The creation timestamp in milliseconds since epoch.
     */
    @Json(name = "dateAdded")
    val dateAdded: Long? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
