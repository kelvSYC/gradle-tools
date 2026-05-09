package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property

/**
 * Client registration info for a Bitbucket Cloud REST API client.
 *
 * The [credentials] property holds the app password credentials, where the username is the Bitbucket username
 * and the password is the app password.
 */
interface BitbucketCloudClientInfo : ServiceClientInfo<BitbucketCloudService> {
    /**
     * The base URL for the Bitbucket Cloud REST API. Defaults to `https://api.bitbucket.org/2.0`.
     */
    val baseUrl: Property<String>

    /**
     * App password credentials for authenticating with the Bitbucket Cloud API.
     */
    val credentials: Property<PasswordCredentials>
}
