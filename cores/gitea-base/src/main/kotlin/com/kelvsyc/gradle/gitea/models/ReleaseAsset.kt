package com.kelvsyc.gradle.gitea.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A release asset attached to a Gitea release.
 */
@JsonClass(generateAdapter = false)
data class ReleaseAsset(
    /**
     * The unique identifier for the asset.
     */
    val id: Long? = null,

    /**
     * The name of the asset file.
     */
    val name: String? = null,

    /**
     * The size of the asset in bytes.
     */
    val size: Long? = null,

    /**
     * The number of times the asset has been downloaded.
     */
    @Json(name = "download_count")
    val downloadCount: Long? = null,

    /**
     * The direct download URL for the asset.
     */
    @Json(name = "browser_download_url")
    val browserDownloadUrl: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
