package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.JsonClass

/**
 * A branch reference in a Bitbucket Cloud repository.
 */
@JsonClass(generateAdapter = false)
data class Branch(
    /**
     * The branch name.
     */
    val name: String? = null,

    /**
     * The object type, typically `branch`.
     */
    val type: String? = null,
)
