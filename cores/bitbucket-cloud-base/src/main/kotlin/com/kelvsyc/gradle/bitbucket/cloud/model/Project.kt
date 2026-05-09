package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.JsonClass

/**
 * A Bitbucket Cloud project that groups repositories within a workspace.
 */
@JsonClass(generateAdapter = false)
data class Project(
    /**
     * The project UUID, including surrounding braces.
     */
    val uuid: String? = null,

    /**
     * The project key (short identifier).
     */
    val key: String? = null,

    /**
     * The project name.
     */
    val name: String? = null,

    /**
     * The object type, typically `project`.
     */
    val type: String? = null,
)
