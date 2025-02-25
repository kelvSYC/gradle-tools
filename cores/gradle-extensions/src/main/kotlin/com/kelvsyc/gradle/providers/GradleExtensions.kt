package com.kelvsyc.gradle.providers

import org.gradle.api.invocation.Gradle

/**
 * Retrieves the root [Gradle] instance.
 *
 * The root [Gradle] instance is the instance for which there is no parent [Gradle].
 */
val Gradle.rootGradle
    get() = generateSequence(this, Gradle::getParent).last()
