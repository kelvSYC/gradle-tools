package com.kelvsyc.gradle.bitbucket.cloud.fixtures

import org.gradle.api.credentials.PasswordCredentials

/**
 * A plain (non-Gradle-managed, non-`Serializable`) implementation of [PasswordCredentials], used to probe
 * whether Gradle's config-cache codec can serialize `Property<PasswordCredentials>` when the value is a
 * direct implementation rather than a managed instance created via `ObjectFactory`.
 *
 * The expected outcome is failure: Gradle's CC codec has no codec for arbitrary implementations of
 * [PasswordCredentials] that are not managed types or `Serializable`. The corresponding test in
 * [com.kelvsyc.gradle.bitbucket.cloud.BuildServiceConfigurationCacheSpec] pins this expectation so that any
 * future relaxation in Gradle's CC codec surfaces immediately.
 */
class FakePasswordCredentials(
    private var username: String? = null,
    private var password: String? = null,
) : PasswordCredentials {
    override fun getUsername(): String? = username
    override fun setUsername(userName: String?) { username = userName }
    override fun getPassword(): String? = password
    override fun setPassword(password: String?) { this.password = password }
}
