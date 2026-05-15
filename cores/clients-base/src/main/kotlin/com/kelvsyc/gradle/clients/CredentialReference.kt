package com.kelvsyc.gradle.clients

/**
 * A reference to where a build service credential can be found at execution time.
 *
 * ## Why this type exists
 *
 * Gradle's configuration cache serializes the resolved value of every [org.gradle.api.provider.Property] on
 * [org.gradle.api.services.BuildServiceParameters] to `.gradle/configuration-cache/` as plaintext when the cache
 * is first written. This happens regardless of whether the caller used
 * [org.gradle.api.provider.ProviderFactory.environmentVariable] or a literal string — the provider chain is
 * fully resolved at cache-write time, and the concrete string is stored on disk. Any user or process with read
 * access to the build directory can extract those values.
 *
 * To keep secrets out of the cache, [BuildServiceParameters][org.gradle.api.services.BuildServiceParameters]
 * properties that hold credentials use `Property<CredentialReference>` instead of `Property<String>`. A
 * [CredentialReference] stores only the *name* of an environment variable or JVM system property — never the
 * secret value itself. Only non-sensitive metadata (a string name) is serialized to the cache.
 *
 * ## Where resolution happens
 *
 * Subclasses of [com.kelvsyc.gradle.clients.AbstractClientBuildService] resolve credentials inside
 * `createClient()`, which is called lazily on first access during task execution — after the configuration
 * cache has already been read and deserialized. The actual secret value is obtained at that point via
 * [System.getenv] or [System.getProperty] and held only in memory for the lifetime of the build service.
 *
 * ## Implementing [Serializable]
 *
 * This sealed class implements [java.io.Serializable] so that Gradle's configuration cache can serialize
 * instances of it (Gradle falls back to Java serialization for custom types held in
 * [BuildServiceParameters][org.gradle.api.services.BuildServiceParameters] properties).
 *
 * @see EnvironmentVariable
 * @see SystemProperty
 */
sealed class CredentialReference : java.io.Serializable {
    /**
     * Resolves the credential from an environment variable at build execution time.
     *
     * Only the variable [name] is serialized to the configuration cache — the value is looked up via
     * [System.getenv] when the build service initializes its client, keeping the secret out of the cache.
     *
     * @param name The name of the environment variable (e.g. `"AWS_SECRET_ACCESS_KEY"`).
     */
    data class EnvironmentVariable(val name: String) : CredentialReference()

    /**
     * Resolves the credential from a JVM system property at build execution time.
     *
     * Only the property [name] is serialized to the configuration cache — the value is looked up via
     * [System.getProperty] when the build service initializes its client, keeping the secret out of the cache.
     *
     * @param name The name of the JVM system property (e.g. `"aws.secretAccessKey"`).
     *
     * ## Using `gradle.properties` as a source
     *
     * Gradle project properties (set in `gradle.properties` or via `-P`) are not accessible from a
     * [build service][org.gradle.api.services.BuildService] at execution time. However, any entry in
     * `gradle.properties` prefixed with `systemProp.` is forwarded by Gradle into the JVM system
     * properties, making it resolvable via [System.getProperty]:
     *
     * ```properties
     * # ~/.gradle/gradle.properties
     * systemProp.bitbucket.token=mytoken123
     * ```
     *
     * ```kotlin
     * parameters.tokenRef.set(CredentialReference.SystemProperty("bitbucket.token"))
     * ```
     *
     * This gives callers a `gradle.properties`-backed credential source that stays completely out of
     * the configuration cache. Contrast with `from(Provider<PasswordCredentials>)` (where available),
     * which resolves credentials during configuration and stores the resolved values in the cache —
     * appropriate for local development on a single machine, but not for shared CI/CD environments.
     */
    data class SystemProperty(val name: String) : CredentialReference()

    /**
     * Stores a credential value directly as a plain string.
     *
     * **WARNING — configuration cache unsafe**: This value is serialized to the Gradle configuration
     * cache in plaintext. Any process with read access to `.gradle/configuration-cache/` can extract it.
     * Prefer [EnvironmentVariable] or [SystemProperty] for all production and CI/CD use.
     *
     * This variant exists to support the `from(Provider<PasswordCredentials>)` family of extension
     * functions, which must resolve Gradle credential objects during the configuration phase because
     * [org.gradle.api.credentials.PasswordCredentials] is a configuration-time construct designed for
     * repository authentication — not for supplying credentials to services that run at task execution
     * time. Those `from()` functions are deprecated for exactly this reason; prefer
     * [EnvironmentVariable] or [SystemProperty] directly. For `gradle.properties`-backed credentials,
     * see [SystemProperty] and the `systemProp.` convention documented there.
     */
    data class Literal(val value: String) : CredentialReference()

    /**
     * Resolves this reference to the actual credential value.
     *
     * @throws IllegalStateException if the referenced environment variable or system property is not set.
     */
    fun resolve(): String = resolveOrNull()
        ?: error("Credential not set: $this")

    /**
     * Resolves this reference to the actual credential value, or `null` if the referenced environment
     * variable or system property is not set.
     */
    fun resolveOrNull(): String? = when (this) {
        is EnvironmentVariable -> System.getenv(name)
        is SystemProperty -> System.getProperty(name)
        is Literal -> value
    }
}
