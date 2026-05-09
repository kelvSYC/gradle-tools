package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PaginatedResponse
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
        test("fetchAllPages - single page with no next link") {
            val call = mockk<Call<PaginatedResponse<String>>>()
            every { call.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a", "b", "c")),
            )

            val result = fetchAllPages(call) { error("should not be called") }

            result shouldHaveSize 3
            result shouldBe listOf("a", "b", "c")
        }

        test("fetchAllPages - follows multiple pages") {
            val call1 = mockk<Call<PaginatedResponse<String>>>()
            every { call1.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a"), next = "http://next/2"),
            )
            val call2 = mockk<Call<PaginatedResponse<String>>>()
            every { call2.execute() } returns Response.success(
                PaginatedResponse(values = listOf("b"), next = "http://next/3"),
            )
            val call3 = mockk<Call<PaginatedResponse<String>>>()
            every { call3.execute() } returns Response.success(
                PaginatedResponse(values = listOf("c")),
            )

            val result = fetchAllPages(call1) { url ->
                when (url) {
                    "http://next/2" -> call2
                    "http://next/3" -> call3
                    else -> error("unexpected URL: $url")
                }
            }

            result shouldBe listOf("a", "b", "c")
        }

        test("fetchAllPages - empty first page") {
            val call = mockk<Call<PaginatedResponse<String>>>()
            every { call.execute() } returns Response.success(
                PaginatedResponse(values = emptyList()),
            )

            val result = fetchAllPages(call) { error("should not be called") }

            result.shouldBeEmpty()
        }

        test("paginatedSequence - lazily fetches pages") {
            val call1 = mockk<Call<PaginatedResponse<String>>>()
            every { call1.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a", "b"), next = "http://next/2"),
            )
            val call2 = mockk<Call<PaginatedResponse<String>>>()
            every { call2.execute() } returns Response.success(
                PaginatedResponse(values = listOf("c", "d")),
            )

            val sequence = paginatedSequence(call1) { call2 }
            val first = sequence.first()

            first shouldBe "a"
            verify(exactly = 1) { call1.execute() }
            verify(exactly = 0) { call2.execute() }
        }

        test("paginatedSequence - take stops fetching early") {
            val call1 = mockk<Call<PaginatedResponse<String>>>()
            every { call1.execute() } returns Response.success(
                PaginatedResponse(values = listOf("a"), next = "http://next/2"),
            )
            val call2 = mockk<Call<PaginatedResponse<String>>>()
            every { call2.execute() } returns Response.success(
                PaginatedResponse(values = listOf("b"), next = "http://next/3"),
            )

            val result = paginatedSequence(call1) { call2 }.take(2).toList()

            result shouldBe listOf("a", "b")
            verify(exactly = 1) { call1.execute() }
            verify(exactly = 1) { call2.execute() }
        }
    }
}
