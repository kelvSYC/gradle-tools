package com.kelvsyc.gradle.bitbucket.cloud.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Paginated response envelope used by the Bitbucket Cloud REST API.
 *
 * @param T The type of elements in the [values] list.
 */
@JsonClass(generateAdapter = false)
data class PaginatedResponse<T>(
    /**
     * The current page number (1-based).
     */
    val page: Int? = null,

    /**
     * The number of items on this page.
     */
    val size: Int? = null,

    /**
     * The maximum number of items per page.
     */
    @Json(name = "pagelen")
    val pageLen: Int? = null,

    /**
     * URL to the next page of results, or `null` if this is the last page.
     */
    val next: String? = null,

    /**
     * URL to the previous page of results, or `null` if this is the first page.
     */
    val previous: String? = null,

    /**
     * The list of items on this page.
     */
    val values: List<T> = emptyList(),
)
