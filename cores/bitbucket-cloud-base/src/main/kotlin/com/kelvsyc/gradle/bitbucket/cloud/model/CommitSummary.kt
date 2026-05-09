package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.JsonClass

/**
 * A lightweight commit reference used in nested contexts such as pull request endpoints and merge commits.
 */
@JsonClass(generateAdapter = false)
data class CommitSummary(
    /**
     * The full commit hash.
     */
    val hash: String? = null,

    /**
     * The object type, typically `commit`.
     */
    val type: String? = null,
)
