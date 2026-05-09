package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A Bitbucket Cloud repository.
 */
@JsonClass(generateAdapter = false)
data class Repository(
    /**
     * The repository UUID, including surrounding braces.
     */
    val uuid: String? = null,

    /**
     * The repository name.
     */
    val name: String? = null,

    /**
     * The repository slug (URL-friendly identifier).
     */
    val slug: String? = null,

    /**
     * The full name in `workspace/repo-slug` format.
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
    @Json(name = "is_private")
    val isPrivate: Boolean? = null,

    /**
     * The repository's main branch.
     */
    @Json(name = "mainbranch")
    val mainBranch: Branch? = null,

    /**
     * The owner of the repository.
     */
    val owner: Account? = null,

    /**
     * The project this repository belongs to, if any.
     */
    val project: Project? = null,

    /**
     * The primary programming language of the repository, as detected by Bitbucket.
     */
    val language: String? = null,

    /**
     * The ISO 8601 timestamp when the repository was created.
     */
    @Json(name = "created_on")
    val createdOn: String? = null,

    /**
     * The ISO 8601 timestamp when the repository was last updated.
     */
    @Json(name = "updated_on")
    val updatedOn: String? = null,

    /**
     * The object type, typically `repository`.
     */
    val type: String? = null,
)
