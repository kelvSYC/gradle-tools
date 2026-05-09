package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.PaginatedResponse
import retrofit2.Call

/**
 * Returns a [Sequence] that lazily fetches pages from a paginated Bitbucket Data Center API response.
 *
 * Each page is fetched only when the sequence is consumed. The [fetchPage] function receives the
 * `start` index for the next page; the first call receives `0` (or the specified [initialStart]).
 *
 * @param T The type of items in the paginated response.
 * @param initialStart The start index for the first page (defaults to `0`).
 * @param fetchPage A function that takes a `start` index and returns a [Call] for that page.
 * @return A [Sequence] yielding all items across all pages.
 */
fun <T> paginatedSequence(
    initialStart: Int = 0,
    fetchPage: (start: Int) -> Call<PaginatedResponse<T>>,
): Sequence<T> = sequence {
    var nextStart: Int? = initialStart
    while (nextStart != null) {
        val response = fetchPage(nextStart).execute().body()
        if (response != null) {
            yieldAll(response.values)
        }
        nextStart = response?.takeIf { it.isLastPage == false }?.nextPageStart
    }
}

/**
 * Collects all items from a paginated Bitbucket Data Center API response.
 *
 * This is a convenience shorthand for `paginatedSequence(initialStart, fetchPage).toList()`.
 *
 * @param T The type of items in the paginated response.
 * @param initialStart The start index for the first page (defaults to `0`).
 * @param fetchPage A function that takes a `start` index and returns a [Call] for that page.
 * @return A list containing all items across all pages.
 */
fun <T> fetchAllPages(
    initialStart: Int = 0,
    fetchPage: (start: Int) -> Call<PaginatedResponse<T>>,
): List<T> = paginatedSequence(initialStart, fetchPage).toList()
