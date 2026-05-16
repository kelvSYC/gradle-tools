package com.kelvsyc.gradle.karakum

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * Project-level configuration for the Karakum code-generation plugin.
 *
 * The default invocation is `npx karakum` (optionally pinned via [karakumVersion]).
 * Call one of the preset methods to switch modes; assign [karakumCommand] directly for exotic
 * configurations not covered by the presets.
 *
 * Analogous to `toolVersion` in JVM code-quality plugins: [karakumVersion] drives the npm package
 * version used in npx-based invocations without requiring a global Karakum installation.
 */
abstract class KarakumExtension @Inject constructor(private val providers: ProviderFactory) {

    /**
     * npm package version to pin for npx-based invocations, e.g. `"1.2.3"` produces
     * `npx karakum@1.2.3`. When unset, the latest published version is used. Has no effect
     * when [useSystem] or [useNodeModules] is active.
     */
    abstract val karakumVersion: Property<String>

    /**
     * Full invocation command tokens. The first token is the executable; any additional tokens
     * are prepended to Karakum's own arguments (e.g. `["npx", "karakum@1.2.3"]`).
     * Normally set via a preset method; assign directly only for exotic configurations.
     */
    abstract val karakumCommand: ListProperty<String>

    /**
     * Invoke `npx karakum[@version]` using the system `npx` binary.
     *
     * This is the bootstrapping-friendly default — no global Karakum installation required.
     * The resolved package is `karakum@[karakumVersion]` when [karakumVersion] is set, or plain
     * `karakum` otherwise.
     */
    fun useNpx() {
        karakumCommand.set(
            karakumVersion.map { listOf("npx", "karakum@$it") }.orElse(listOf("npx", "karakum"))
        )
    }

    /**
     * Invoke `npx karakum[@version]` via [npxBinary].
     *
     * Use this to route through the `npx` executable managed by the Kotlin Multiplatform plugin's
     * Node.js toolchain rather than the system binary. The plugin wires this automatically when
     * a JS target is detected; call it explicitly only when custom routing is needed.
     */
    fun useNpx(npxBinary: Provider<RegularFile>) {
        karakumCommand.set(
            npxBinary.zip(karakumVersion.map { "karakum@$it" }.orElse("karakum")) { npx, pkg ->
                listOf(npx.asFile.absolutePath, pkg)
            }
        )
    }

    /**
     * Invoke `node_modules/.bin/karakum` directly.
     *
     * Requires Karakum to be listed as a `devDependency` in `package.json` so that
     * `kotlinNpmInstall` places the wrapper script in `node_modules/.bin/`. Offers fully
     * reproducible builds because the exact version is pinned in `package.json`.
     */
    fun useNodeModules() {
        karakumCommand.set(listOf("node_modules/.bin/karakum"))
    }

    /**
     * Invoke `node_modules/.bin/karakum` via [nodeBinary].
     *
     * Combines the reproducibility of [useNodeModules] with explicit routing through a specific
     * `node` executable — typically the one managed by the Kotlin Multiplatform plugin's toolchain.
     */
    fun useNodeModules(nodeBinary: Provider<RegularFile>) {
        karakumCommand.set(
            nodeBinary.map { node ->
                listOf(node.asFile.absolutePath, "node_modules/.bin/karakum")
            }
        )
    }

    /**
     * Invoke the `karakum` binary resolved from `PATH`.
     *
     * Use this when Karakum is installed globally (e.g. in a CI environment with a pre-configured
     * tool cache). Requires no Node.js management but ties the build to an externally managed tool.
     */
    fun useSystem() {
        karakumCommand.set(providers.which("karakum").map { listOf(it) })
    }
}
