package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PaginatedResponse
import retrofit2.Call

/**
 * Returns a [Sequence] that lazily fetches pages from a paginated Bitbucket Cloud API response.
 *
 * Each page is fetched only when the sequence is consumed, making this suitable for large result sets
 * where eagerly fetching all pages would be wasteful. Items are yielded one at a time from each page;
 * the next page is not fetched until all items from the current page have been consumed.
 *
 * @param T The type of items in the paginated response.
 * @param firstPage A [Call] that fetches the first page.
 * @param nextPage A function that takes a `next` URL and returns a [Call] for that page.
 * @return A [Sequence] yielding all items across all pages.
 */
fun <T> paginatedSequence(
    firstPage: Call<PaginatedResponse<T>>,
    nextPage: (String) -> Call<PaginatedResponse<T>>,
): Sequence<T> = sequence {
    var response = firstPage.execute().body()
    while (response != null) {
        yieldAll(response.values)
        val nextUrl = response.next ?: break
        response = nextPage(nextUrl).execute().body()
    }
}

/**
 * Collects all items from a paginated Bitbucket Cloud API response by following `next` links.
 *
 * This is a convenience shorthand for `paginatedSequence(firstPage, nextPage).toList()`.
 *
 * @param T The type of items in the paginated response.
 * @param firstPage A [Call] that fetches the first page.
 * @param nextPage A function that takes a `next` URL and returns a [Call] for that page.
 * @return A list containing all items across all pages.
 */
fun <T> fetchAllPages(
    firstPage: Call<PaginatedResponse<T>>,
    nextPage: (String) -> Call<PaginatedResponse<T>>,
): List<T> = paginatedSequence(firstPage, nextPage).toList()
