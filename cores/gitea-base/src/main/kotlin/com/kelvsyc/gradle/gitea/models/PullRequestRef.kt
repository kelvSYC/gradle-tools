package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A reference to a branch in a pull request (head or base).
 */
@JsonClass(generateAdapter = false)
data class PullRequestRef(
    /**
     * The branch label (e.g., `owner:branch-name`).
     */
    val label: String? = null,

    /**
     * The branch name (ref).
     */
    val ref: String? = null,

    /**
     * The commit SHA for this reference.
     */
    val sha: String? = null,

    /**
     * The repository containing this reference.
     */
    val repo: Repository? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
