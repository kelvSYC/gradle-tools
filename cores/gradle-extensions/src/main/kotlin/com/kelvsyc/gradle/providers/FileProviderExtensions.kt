package com.kelvsyc.gradle.providers

import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

/**
 * Returns the absolute path for this file location.
 *
 * Syntactic shorthand for `map { it.asFile.absolutePath }`
 */
val Provider<out FileSystemLocation>.asAbsolutePath
    get() = map { it.asFile.absolutePath }

/**
 * Returns the file location as a [Path][java.nio.file.Path] object.
 *
 * Syntactic shorthand for `map { it.asFile.toPath }`
 */
val Provider<out FileSystemLocation>.asPath
    get() = map { it.asFile.toPath() }

/**
 * Returns a [Provider] whose `Directory` value is the location is the given path, resolved to this `Directory` value.
 *
 * Syntactic shorthand for `map { it.dir(path) }`
 *
 * This function extends the semantics of [DirectoryProperty.dir][org.gradle.api.file.DirectoryProperty.dir] to
 * generic providers of [Directory][org.gradle.api.file.Directory], and thus will never be used on objects with a
 * declared type of [DirectoryProperty][org.gradle.api.file.DirectoryProperty].
 */
fun Provider<Directory>.dir(path: String) = map { it.dir(path) }

/**
 * Returns a [Provider] whose `Directory` value is the location is the given path, resolved to this `Directory` value.
 *
 * Syntactic shorthand for `flatMap { it.dir(path) }`
 *
 * This function extends the semantics of [DirectoryProperty.dir][org.gradle.api.file.DirectoryProperty.dir] to
 * generic providers of [Directory][org.gradle.api.file.Directory], and thus will never be used on objects with a
 * declared type of [DirectoryProperty][org.gradle.api.file.DirectoryProperty].
 */
fun Provider<Directory>.dir(path: Provider<String>) = flatMap { it.dir(path) }

/**
 * Returns a [Provider] whose `RegularFile` value is the location is the given path, resolved to this `Directory` value.
 *
 * Syntactic shorthand for `map { it.file(path) }`
 *
 * This function extends the semantics of [DirectoryProperty.file][org.gradle.api.file.DirectoryProperty.file] to
 * generic providers of [Directory][org.gradle.api.file.Directory], and thus will never be used on objects with a
 * declared type of [DirectoryProperty][org.gradle.api.file.DirectoryProperty].
 */
fun Provider<Directory>.file(path: String) = map { it.file(path) }

/**
 * Returns a [Provider] whose `RegularFile` value is the location is the given path, resolved to this `Directory` value.
 *
 * Syntactic shorthand for `flatMap { it.file(path) }`
 *
 * This function extends the semantics of [DirectoryProperty.file][org.gradle.api.file.DirectoryProperty.file] to
 * generic providers of [Directory][org.gradle.api.file.Directory], and thus will never be used on objects with a
 * declared type of [DirectoryProperty][org.gradle.api.file.DirectoryProperty].
 */
fun Provider<Directory>.file(path: Provider<String>) = flatMap { it.file(path) }
