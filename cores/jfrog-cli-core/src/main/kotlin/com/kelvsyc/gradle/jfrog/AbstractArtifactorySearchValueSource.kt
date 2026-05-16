package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Base [ValueSource] implementation that runs `jf rt search` and exposes the JSON output for subclasses to parse.
 *
 * Subclasses must implement [buildSearchArgs] to supply the search-specific CLI arguments (pattern, file spec path,
 * or AQL flags), and [doObtain] to transform the raw JSON string into the desired type.
 *
 * **Configuration cache — access token:** If [Parameters.accessTokenRef] is set, only the lookup reference
 * (environment variable name or system property key) is serialized to `.gradle/configuration-cache/` — never
 * the token value itself. The token is resolved via [CredentialReference.resolve] inside [obtain], which runs
 * at configuration time but leaves no trace of the raw value in the cache. Prefer leaving
 * [Parameters.accessTokenRef] unset and configuring credentials in the JFrog CLI itself (via `jf config add`)
 * to avoid token resolution at configuration time entirely.
 *
 * **Configuration cache — search results:** Gradle serializes the result of every [ValueSource.obtain] call to
 * the configuration cache in plaintext. Whatever [doObtain] returns will be stored in
 * `.gradle/configuration-cache/`. Search results are typically artifact metadata (names, checksums, paths) and
 * are not sensitive. However, storing the resulting [org.gradle.api.provider.Provider] in any task field —
 * `@Input`, `@get:Internal`, or a private `val` — causes `obtain()` to run at configuration time.
 *
 * @param T The type of value produced by this source.
 * @param P The parameters type, which must extend [Parameters].
 */
abstract class AbstractArtifactorySearchValueSource<T : Any, P : AbstractArtifactorySearchValueSource.Parameters>
@Inject constructor(private val execOperations: ExecOperations) : ValueSource<T, P> {
    /**
     * Common parameters for all Artifactory search value sources.
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The underlying JFrog CLI command.
         */
        @get:Internal
        val jfCommand: Property<String>

        /**
         * The Artifactory server URL. Leave unset to use the CLI's configured default server.
         */
        @get:Input
        @get:Optional
        val serverUrl: Property<String>

        /**
         * A reference to the JFrog access token for authentication. Leave unset to use the CLI's configured
         * credentials. Set to a [CredentialReference.EnvironmentVariable] or [CredentialReference.SystemProperty]
         * pointing to the token — the lookup name, not the token value, is what is stored in the configuration cache.
         */
        @get:Internal
        val accessTokenRef: Property<CredentialReference>
    }

    /**
     * Returns the search-specific arguments to append after the common `jf rt search` flags.
     *
     * Implementations typically return a pattern (e.g. `listOf("libs-release-local/app-1.0.jar")`)
     * or a file spec reference (e.g. `listOf("--spec", specFile.absolutePath)`).
     */
    protected abstract fun buildSearchArgs(): List<String>

    /**
     * Transforms the raw JSON output of `jf rt search` into the desired value.
     *
     * @param output The full stdout of the search command as a UTF-8 string.
     * @return The parsed value, or `null` if no value could be obtained.
     */
    protected abstract fun doObtain(output: String): T?

    final override fun obtain(): T? {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            executable(parameters.jfCommand.get())
            val args = buildList {
                add("rt")
                add("search")
                if (parameters.serverUrl.isPresent) {
                    add("--url")
                    add(parameters.serverUrl.get())
                }
                if (parameters.accessTokenRef.isPresent) {
                    add("--access-token")
                    add(parameters.accessTokenRef.get().resolve())
                }
                addAll(buildSearchArgs())
            }
            args(args)
            standardOutput = output
        }
        return doObtain(output.toString(Charsets.UTF_8))
    }
}
