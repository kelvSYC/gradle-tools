package com.kelvsyc.gradle.moshi

import com.squareup.moshi.Moshi

/**
 * Registers Moshi adapters for common Gradle and JDK types with this [Moshi.Builder].
 *
 * Adapters registered:
 * - [UriAdapter] — serializes [java.net.URI] as a string
 * - [FileAdapter] — serializes [java.io.File] as an absolute path string
 */
fun Moshi.Builder.addGradleTypeAdapters(): Moshi.Builder = apply {
    add(UriAdapter())
    add(FileAdapter())
}
