package com.kelvsyc.gradle

import com.kelvsyc.gradle.internal.RootGradleDelegate
import org.gradle.api.invocation.Gradle

/**
 * Retrieves the root [Gradle] instance.
 *
 * The root [Gradle] instance is the instance for which there is no parent [Gradle].
 */
val Gradle.rootGradle by RootGradleDelegate
