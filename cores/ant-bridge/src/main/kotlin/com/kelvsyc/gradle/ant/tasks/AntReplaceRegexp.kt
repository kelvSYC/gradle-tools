package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Ant's `replaceregexp` over a copy of [sourceDirectory]: the filtered tree is synced into [outputDirectory] and the
 * replacement runs there, so the inputs are never modified.
 */
@CacheableTask
abstract class AntReplaceRegexp : AbstractAntTask() {
    /** The directory whose files are processed. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    /** Ant include patterns; every file by default. */
    @get:Input
    abstract val includes: SetProperty<String>

    /** Ant exclude patterns. */
    @get:Input
    abstract val excludes: SetProperty<String>

    /** The regular expression to match (Ant's `match`). */
    @get:Input
    abstract val match: Property<String>

    /** The replacement (Ant's `replace`). */
    @get:Input
    abstract val replace: Property<String>

    /** The regular expression flags (Ant's `flags`), such as `gi`. */
    @get:Input
    abstract val flags: Property<String>

    /** Whether to match line by line (Ant's `byline`). */
    @get:Input
    abstract val byLine: Property<Boolean>

    /** The file encoding (Ant's `encoding`), or the platform default if unset. */
    @get:Input
    @get:Optional
    abstract val encoding: Property<String>

    /** The directory receiving the processed copy; must not overlap [sourceDirectory]. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        includes.convention(listOf("**/*"))
        excludes.convention(emptyList())
        flags.convention("")
        byLine.convention(false)
    }

    override fun prepare() {
        // Resolved up front: inside the spec lambdas, `includes` and `excludes` mean the spec's own properties.
        val included = includes.get()
        val excluded = excludes.get()
        val source = sourceDirectory
        val target = outputDirectory
        fileSystemOperations.sync {
            from(source) {
                include(included)
                exclude(excluded)
            }
            into(target)
        }
    }

    override fun elements(): List<AntElement> {
        val fileset = element(
            "fileset",
            "dir" to outputDirectory,
            "includes" to includes.get().joinToString(","),
            "excludes" to excludes.get().takeIf { it.isNotEmpty() }?.joinToString(",")
        )
        val regexp = element(
            "replaceregexp",
            "match" to match,
            "replace" to replace,
            "flags" to flags,
            "byline" to byLine,
            "encoding" to encoding.orNull
        )
        regexp.children.add(fileset)
        return listOf(regexp)
    }
}
