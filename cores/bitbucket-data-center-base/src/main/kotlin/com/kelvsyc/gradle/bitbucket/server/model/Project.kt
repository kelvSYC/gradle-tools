package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.JsonClass

/**
 * A Bitbucket Data Center project.
 */
@JsonClass(generateAdapter = false)
data class Project(
    /**
     * The project's numeric ID.
     */
    val id: Long? = null,

    /**
     * The project key (short uppercase identifier).
     */
    val key: String? = null,

    /**
     * The project name.
     */
    val name: String? = null,

    /**
     * The project description.
     */
    val description: String? = null,

    /**
     * Whether the project is publicly accessible.
     */
    val public: Boolean? = null,

    /**
     * The object type, typically `NORMAL`.
     */
    val type: String? = null,
)
