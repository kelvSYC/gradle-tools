@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.gradle.logging

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

/**
 * Logs a message at the given log level.
 */
fun Logger.log(level: LogLevel, message: () -> String) {
    if (isEnabled(level)) {
        log(level, message())
    }
}

/**
 * Logs a message at the given log level.
 */
fun Logger.log(level: LogLevel, e: Throwable, message: () -> String) {
    if (isEnabled(level)) {
        log(level, message(), e)
    }
}

/**
 * Logs a message at the `DEBUG` level.
 */
fun Logger.debug(message: () -> String) = log(LogLevel.DEBUG, message)

/**
 * Logs a message at the `DEBUG` level.
 */
fun Logger.debug(e: Throwable, message: () -> String) = log(LogLevel.DEBUG, e, message)

/**
 * Logs a message at the `ERROR` level.
 */
fun Logger.error(message: () -> String) = log(LogLevel.ERROR, message)

/**
 * Logs a message at the `ERROR` level.
 */
fun Logger.error(e: Throwable, message: () -> String) = log(LogLevel.ERROR, e, message)

/**
 * Logs a message at the `INFO` level.
 */
fun Logger.info(message: () -> String) = log(LogLevel.INFO, message)

/**
 * Logs a message at the `INFO` level.
 */
fun Logger.info(e: Throwable, message: () -> String) = log(LogLevel.INFO, e, message)

/**
 * Logs a message at the `LIFECYCLE` level.
 */
fun Logger.lifecycle(message: () -> String) = log(LogLevel.LIFECYCLE, message)

/**
 * Logs a message at the `LIFECYCLE` level.
 */
fun Logger.lifecycle(e: Throwable, message: () -> String) = log(LogLevel.LIFECYCLE, e, message)

/**
 * Logs a message at the `QUIET` level.
 */
fun Logger.quiet(message: () -> String) = log(LogLevel.QUIET, message)

/**
 * Logs a message at the `QUIET` level.
 */
fun Logger.quiet(e: Throwable, message: () -> String) = log(LogLevel.QUIET, e, message)

/**
 * Logs a message at the `WARN` level.
 */
fun Logger.warn(message: () -> String) = log(LogLevel.WARN, message)

/**
 * Logs a message at the `WARN` level.
 */
fun Logger.warn(e: Throwable, message: () -> String) = log(LogLevel.WARN, e, message)
