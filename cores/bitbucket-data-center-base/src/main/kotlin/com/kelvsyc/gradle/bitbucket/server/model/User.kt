package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A Bitbucket Data Center user account.
 */
@JsonClass(generateAdapter = false)
data class User(
    /**
     * The user's numeric ID.
     */
    val id: Long? = null,

    /**
     * The user's login name.
     */
    val name: String? = null,

    /**
     * The user's display name.
     */
    @Json(name = "displayName")
    val displayName: String? = null,

    /**
     * The user's email address.
     */
    @Json(name = "emailAddress")
    val emailAddress: String? = null,

    /**
     * The user's URL-friendly slug.
     */
    val slug: String? = null,

    /**
     * The object type, typically `NORMAL`.
     */
    val type: String? = null,

    /**
     * Whether the user account is active.
     */
    val active: Boolean? = null,
)
