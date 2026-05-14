package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A lightweight repository reference used in nested contexts such as pull request endpoints.
 */
@JsonClass(generateAdapter = false)
data class RepositorySummary(
    /**
     * The repository UUID, including surrounding braces.
     */
    val uuid: String? = null,

    /**
     * The repository name.
     */
    val name: String? = null,

    /**
     * The full name in `workspace/repo-slug` format.
     */
    @Json(name = "full_name")
    val fullName: String? = null,

    /**
     * The object type, typically `repository`.
     */
    val type: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
