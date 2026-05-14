package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

/**
 * A Bitbucket Cloud user or team account.
 */
@JsonClass(generateAdapter = false)
data class Account(
    /**
     * The account UUID, including surrounding braces.
     */
    val uuid: String? = null,

    /**
     * The account's display name.
     */
    @Json(name = "display_name")
    val displayName: String? = null,

    /**
     * The account type, typically `user` or `team`.
     */
    val type: String? = null,

    /**
     * The account's username (may not be present for all account types).
     */
    val nickname: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
