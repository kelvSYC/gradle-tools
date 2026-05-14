package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A ref (branch) endpoint of a pull request in Bitbucket Data Center.
 */
@JsonClass(generateAdapter = false)
data class PullRequestRef(
    /**
     * The full ref path (e.g. `refs/heads/feature/my-branch`).
     */
    val id: String? = null,

    /**
     * The short display name of the ref (e.g. `feature/my-branch`).
     */
    @Json(name = "displayId")
    val displayId: String? = null,

    /**
     * The latest commit hash on this ref.
     */
    @Json(name = "latestCommit")
    val latestCommit: String? = null,

    /**
     * The repository containing this ref.
     */
    val repository: Repository? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
