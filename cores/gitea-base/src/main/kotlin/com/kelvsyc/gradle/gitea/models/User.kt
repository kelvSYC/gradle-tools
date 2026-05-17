package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Gitea user account.
 */
@JsonClass(generateAdapter = false)
data class User(
    /**
     * The unique identifier for the user.
     */
    val id: Long? = null,

    /**
     * The user's login username.
     */
    val login: String? = null,

    /**
     * The user's full name.
     */
    @param:Json(name = "full_name")
    val fullName: String? = null,

    /**
     * The user's email address.
     */
    val email: String? = null,

    /**
     * The URL to the user's avatar image.
     */
    @param:Json(name = "avatar_url")
    val avatarUrl: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
