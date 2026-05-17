package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A release in a Gitea repository.
 */
@JsonClass(generateAdapter = false)
data class Release(
    /**
     * The unique identifier for the release.
     */
    val id: Long? = null,

    /**
     * The Git tag name for the release.
     */
    @param:Json(name = "tag_name")
    val tagName: String? = null,

    /**
     * The release name.
     */
    val name: String? = null,

    /**
     * The release description body.
     */
    val body: String? = null,

    /**
     * Whether the release is marked as a draft.
     */
    val draft: Boolean? = null,

    /**
     * Whether the release is marked as a pre-release.
     */
    val prerelease: Boolean? = null,

    /**
     * The list of assets attached to the release.
     */
    val assets: List<ReleaseAsset>? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
