package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Gitea repository.
 */
@JsonClass(generateAdapter = false)
data class Repository(
    /**
     * The unique identifier for the repository.
     */
    val id: Long? = null,

    /**
     * The repository name.
     */
    val name: String? = null,

    /**
     * The full name of the repository in `owner/repo` format.
     */
    @Json(name = "full_name")
    val fullName: String? = null,

    /**
     * The repository description.
     */
    val description: String? = null,

    /**
     * Whether the repository is private.
     */
    val private: Boolean? = null,

    /**
     * Whether the repository is a fork.
     */
    val fork: Boolean? = null,

    /**
     * The URL to view the repository in Gitea's web interface.
     */
    @Json(name = "html_url")
    val htmlUrl: String? = null,

    /**
     * The Git clone URL for the repository.
     */
    @Json(name = "clone_url")
    val cloneUrl: String? = null,

    /**
     * The name of the default branch.
     */
    @Json(name = "default_branch")
    val defaultBranch: String? = null,

    /**
     * The owner of the repository.
     */
    val owner: User? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
