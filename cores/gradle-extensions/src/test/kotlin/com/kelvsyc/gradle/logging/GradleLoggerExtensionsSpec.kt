package com.kelvsyc.gradle.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

class GradleLoggerExtensionsSpec : FunSpec() {
    init {
        test("log with level - message evaluated and logged when enabled") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.INFO) } returns true
            every { logger.log(LogLevel.INFO, any<String>()) } just runs

            logger.log(LogLevel.INFO) { "test message" }

            verify { logger.log(LogLevel.INFO, "test message") }
        }

        test("log with level - message not evaluated when disabled") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.DEBUG) } returns false
            var lambdaEvaluated = false

            logger.log(LogLevel.DEBUG) {
                lambdaEvaluated = true
                "test message"
            }

            lambdaEvaluated.shouldBeFalse()
        }

        test("log with level and throwable - message evaluated and logged when enabled") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.ERROR) } returns true
            every { logger.log(LogLevel.ERROR, any<String>(), any<Throwable>()) } just runs

            logger.log(LogLevel.ERROR, ex) { "error occurred" }

            verify { logger.log(LogLevel.ERROR, "error occurred", ex) }
        }

        test("log with level and throwable - message not evaluated when disabled") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.ERROR) } returns false
            var lambdaEvaluated = false

            logger.log(LogLevel.ERROR, ex) {
                lambdaEvaluated = true
                "error occurred"
            }

            lambdaEvaluated.shouldBeFalse()
        }

        test("debug delegates to log at DEBUG level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.DEBUG) } returns true
            every { logger.log(LogLevel.DEBUG, any<String>()) } just runs

            logger.debug { "debug message" }

            verify { logger.log(LogLevel.DEBUG, "debug message") }
        }

        test("debug with throwable delegates to log at DEBUG level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.DEBUG) } returns true
            every { logger.log(LogLevel.DEBUG, any<String>(), any<Throwable>()) } just runs

            logger.debug(ex) { "debug message" }

            verify { logger.log(LogLevel.DEBUG, "debug message", ex) }
        }

        test("error delegates to log at ERROR level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.ERROR) } returns true
            every { logger.log(LogLevel.ERROR, any<String>()) } just runs

            logger.error { "error message" }

            verify { logger.log(LogLevel.ERROR, "error message") }
        }

        test("error with throwable delegates to log at ERROR level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.ERROR) } returns true
            every { logger.log(LogLevel.ERROR, any<String>(), any<Throwable>()) } just runs

            logger.error(ex) { "error message" }

            verify { logger.log(LogLevel.ERROR, "error message", ex) }
        }

        test("info delegates to log at INFO level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.INFO) } returns true
            every { logger.log(LogLevel.INFO, any<String>()) } just runs

            logger.info { "info message" }

            verify { logger.log(LogLevel.INFO, "info message") }
        }

        test("info with throwable delegates to log at INFO level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.INFO) } returns true
            every { logger.log(LogLevel.INFO, any<String>(), any<Throwable>()) } just runs

            logger.info(ex) { "info message" }

            verify { logger.log(LogLevel.INFO, "info message", ex) }
        }

        test("lifecycle delegates to log at LIFECYCLE level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.LIFECYCLE) } returns true
            every { logger.log(LogLevel.LIFECYCLE, any<String>()) } just runs

            logger.lifecycle { "lifecycle message" }

            verify { logger.log(LogLevel.LIFECYCLE, "lifecycle message") }
        }

        test("lifecycle with throwable delegates to log at LIFECYCLE level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.LIFECYCLE) } returns true
            every { logger.log(LogLevel.LIFECYCLE, any<String>(), any<Throwable>()) } just runs

            logger.lifecycle(ex) { "lifecycle message" }

            verify { logger.log(LogLevel.LIFECYCLE, "lifecycle message", ex) }
        }

        test("quiet delegates to log at QUIET level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.QUIET) } returns true
            every { logger.log(LogLevel.QUIET, any<String>()) } just runs

            logger.quiet { "quiet message" }

            verify { logger.log(LogLevel.QUIET, "quiet message") }
        }

        test("quiet with throwable delegates to log at QUIET level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.QUIET) } returns true
            every { logger.log(LogLevel.QUIET, any<String>(), any<Throwable>()) } just runs

            logger.quiet(ex) { "quiet message" }

            verify { logger.log(LogLevel.QUIET, "quiet message", ex) }
        }

        test("warn delegates to log at WARN level") {
            val logger = mockk<Logger>()
            every { logger.isEnabled(LogLevel.WARN) } returns true
            every { logger.log(LogLevel.WARN, any<String>()) } just runs

            logger.warn { "warn message" }

            verify { logger.log(LogLevel.WARN, "warn message") }
        }

        test("warn with throwable delegates to log at WARN level") {
            val logger = mockk<Logger>()
            val ex = RuntimeException("boom")
            every { logger.isEnabled(LogLevel.WARN) } returns true
            every { logger.log(LogLevel.WARN, any<String>(), any<Throwable>()) } just runs

            logger.warn(ex) { "warn message" }

            verify { logger.log(LogLevel.WARN, "warn message", ex) }
        }
    }
}
