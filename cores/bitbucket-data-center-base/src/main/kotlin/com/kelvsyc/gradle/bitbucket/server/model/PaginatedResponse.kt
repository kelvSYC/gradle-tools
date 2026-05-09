package com.kelvsyc.gradle.bitbucket.server.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Paginated response envelope used by the Bitbucket Data Center REST API.
 *
 * Pagination is controlled by `start` and `limit` query parameters. When [isLastPage] is `false`,
 * the next page can be fetched by passing [nextPageStart] as the `start` parameter.
 *
 * @param T The type of elements in the [values] list.
 */
@JsonClass(generateAdapter = false)
data class PaginatedResponse<T>(
    /**
     * The index of the first item in this page (0-based).
     */
    val start: Int? = null,

    /**
     * The number of items requested per page.
     */
    val limit: Int? = null,

    /**
     * The number of items actually returned on this page.
     */
    val size: Int? = null,

    /**
     * Whether this is the last page of results.
     */
    @Json(name = "isLastPage")
    val isLastPage: Boolean? = null,

    /**
     * The start index to use when requesting the next page. Only present when [isLastPage] is `false`.
     */
    @Json(name = "nextPageStart")
    val nextPageStart: Int? = null,

    /**
     * The list of items on this page.
     */
    val values: List<T> = emptyList(),
)
