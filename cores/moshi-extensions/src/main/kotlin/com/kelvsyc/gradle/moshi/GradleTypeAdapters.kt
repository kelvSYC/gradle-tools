package com.kelvsyc.gradle.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.io.File
import java.net.URI

/**
 * Moshi adapter for [URI], serializing as a string.
 */
class UriAdapter {
    /**
     * Deserializes a [URI] from its string representation.
     */
    @FromJson
    fun fromJson(value: String): URI = URI.create(value)

    /**
     * Serializes a [URI] to its string representation.
     */
    @ToJson
    fun toJson(uri: URI): String = uri.toString()
}

/**
 * Moshi adapter for [File], serializing as an absolute path string.
 */
class FileAdapter {
    /**
     * Deserializes a [File] from its path string.
     */
    @FromJson
    fun fromJson(value: String): File = File(value)

    /**
     * Serializes a [File] to its absolute path.
     */
    @ToJson
    fun toJson(file: File): String = file.absolutePath
}
