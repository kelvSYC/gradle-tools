package com.kelvsyc.gradle.internal

import org.gradle.api.invocation.Gradle

/**
 * Delegate holder object used to power [Gradle].[rootGradle][com.kelvsyc.gradle.rootGradle]
 */
object RootGradleDelegate : AbstractCachingDelegate<Gradle, Gradle>({
    generateSequence(it, Gradle::getParent).last()
})
