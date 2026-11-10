package com.kelvsyc.gradle.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.IOException
import java.util.Optional

/**
 * Returns this directory as a Git [Repository] object, if this directory represents the working tree directory of a Git
 * repository.
 *
 * In other words, we can expect that, if the directory represents a Git repository, then the comparison below is true:
 * ```
 * asFile == asRepository?.workTree
 * ```
 */
val Directory.asRepository
    get() = try {
        FileRepositoryBuilder().apply {
            workTree = asFile
            isMustExist = true
        }.build()
    } catch (_: IOException) {
        null
    }

/**
 * Returns this directory as a Git [Repository] object. The returned [Provider] is absent if this directory does not
 * represent a Git repository.
 *
 * The directory is treated as the work tree directory of the Git repository.
 */
val Provider<Directory>.asRepository: Provider<Repository>
    // A null result makes the provider absent, but Gradle 8.14's API declares the transformer's result non-null, so a
    // plain nullable result does not compile. Optional.orElse returns a platform type, which passes null through
    // without a Kotlin null check.
    get() = map { Optional.ofNullable(it.asRepository).orElse(null) }
