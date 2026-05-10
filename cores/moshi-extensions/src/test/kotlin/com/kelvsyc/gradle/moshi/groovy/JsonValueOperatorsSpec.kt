package com.kelvsyc.gradle.moshi.groovy

import com.kelvsyc.kotlin.moshi.JsonArray
import com.kelvsyc.kotlin.moshi.JsonBoolean
import com.kelvsyc.kotlin.moshi.JsonNull
import com.kelvsyc.kotlin.moshi.JsonNumber
import com.kelvsyc.kotlin.moshi.JsonObject
import com.kelvsyc.kotlin.moshi.JsonString
import com.kelvsyc.kotlin.moshi.JsonValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class JsonValueOperatorsSpec : FunSpec({
    val json: JsonValue = JsonObject(
        mapOf(
            "name" to JsonString("Alice"),
            "scores" to JsonArray(
                listOf(JsonNumber(10.0), JsonNumber(20.0), JsonNumber(30.0))
            ),
            "address" to JsonObject(
                mapOf("city" to JsonString("Vancouver"))
            ),
        )
    )

    context("get by key") {
        test("returns value from JsonObject") {
            json["name"] shouldBe JsonString("Alice")
        }

        test("returns null for missing key") {
            json["missing"].shouldBeNull()
        }

        test("returns null when called on JsonArray") {
            val arr: JsonValue = JsonArray(listOf(JsonString("a")))
            arr["key"].shouldBeNull()
        }

        test("returns null when called on scalar") {
            val str: JsonValue = JsonString("hello")
            str["key"].shouldBeNull()
        }
    }

    context("get by index") {
        test("returns element from JsonArray") {
            val arr: JsonValue = json["scores"]!!
            arr[0] shouldBe JsonNumber(10.0)
            arr[2] shouldBe JsonNumber(30.0)
        }

        test("returns null for out-of-bounds index") {
            val arr: JsonValue = json["scores"]!!
            arr[99].shouldBeNull()
        }

        test("returns null when called on JsonObject") {
            json[0].shouldBeNull()
        }

        test("returns null when called on scalar") {
            val num: JsonValue = JsonNumber(42.0)
            num[0].shouldBeNull()
        }
    }

    context("chained navigation") {
        test("navigates nested structures like JsonSlurper") {
            json["address"]?.get("city")?.asString() shouldBe "Vancouver"
        }

        test("navigates into arrays within objects") {
            json["scores"]?.get(1)?.asNumber() shouldBe 20.0
        }

        test("returns null for invalid path through scalar") {
            json["name"]?.get("nested").shouldBeNull()
        }

        test("returns null for deeply invalid path") {
            json["missing"]?.get("deeply")?.get("nested").shouldBeNull()
        }
    }
})
