package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Bitbucket Data Center repository.
 */
@JsonClass(generateAdapter = false)
data class Repository(
    /**
     * The repository's numeric ID.
     */
    val id: Long? = null,

    /**
     * The repository slug (URL-friendly identifier).
     */
    val slug: String? = null,

    /**
     * The repository name.
     */
    val name: String? = null,

    /**
     * The repository description.
     */
    val description: String? = null,

    /**
     * The repository state, typically `AVAILABLE`.
     */
    val state: String? = null,

    /**
     * The SCM type, typically `git`.
     */
    @Json(name = "scmId")
    val scmId: String? = null,

    /**
     * Whether the repository is forkable.
     */
    val forkable: Boolean? = null,

    /**
     * Whether the repository is publicly accessible.
     */
    val public: Boolean? = null,

    /**
     * The project this repository belongs to.
     */
    val project: Project? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
