package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.PaginatedResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import retrofit2.Call
import retrofit2.Response

class PaginationUtilsSpec : FunSpec() {
    init {
        test("fetchAllPages - single page with isLastPage true") {
            val call = mockk<Call<PaginatedResponse<String>>>()
            every { call.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a", "b"), isLastPage = true),
            )

            val result = fetchAllPages { call }

            result shouldHaveSize 2
            result shouldBe listOf("a", "b")
        }

        test("fetchAllPages - follows multiple pages via nextPageStart") {
            val call1 = mockk<Call<PaginatedResponse<String>>>()
            every { call1.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a"), isLastPage = false, nextPageStart = 1),
            )
            val call2 = mockk<Call<PaginatedResponse<String>>>()
            every { call2.execute() } returns Response.success(
                PaginatedResponse(values = listOf("b"), isLastPage = false, nextPageStart = 2),
            )
            val call3 = mockk<Call<PaginatedResponse<String>>>()
            every { call3.execute() } returns Response.success(
                PaginatedResponse(values = listOf("c"), isLastPage = true),
            )

            val result = fetchAllPages { start ->
                when (start) {
                    0 -> call1
                    1 -> call2
                    2 -> call3
                    else -> error("unexpected start: $start")
                }
            }

            result shouldBe listOf("a", "b", "c")
        }

        test("fetchAllPages - empty first page") {
            val call = mockk<Call<PaginatedResponse<String>>>()
            every { call.execute() } returns Response.success(
                PaginatedResponse(values = emptyList(), isLastPage = true),
            )

            val result = fetchAllPages { call }

            result.shouldBeEmpty()
        }

        test("paginatedSequence - lazily fetches pages") {
            val call1 = mockk<Call<PaginatedResponse<String>>>()
            every { call1.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a", "b"), isLastPage = false, nextPageStart = 2),
            )
            val call2 = mockk<Call<PaginatedResponse<String>>>()
            every { call2.execute() } returns Response.success(
                PaginatedResponse(values = listOf("c"), isLastPage = true),
            )

            val sequence = paginatedSequence { start ->
                when (start) {
                    0 -> call1
                    else -> call2
                }
            }
            val first = sequence.first()

            first shouldBe "a"
            verify(exactly = 1) { call1.execute() }
            verify(exactly = 0) { call2.execute() }
        }

        test("paginatedSequence - stops when isLastPage is null") {
            val call = mockk<Call<PaginatedResponse<String>>>()
            every { call.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a"), isLastPage = null),
            )

            val result = paginatedSequence { call }.toList()

            result shouldBe listOf("a")
        }
    }
}
