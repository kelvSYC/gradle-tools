package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

/**
 * Client registration info for a Bitbucket Data Center REST API client.
 *
 * The [baseUrl] must point to the server root (e.g. `https://bitbucket.example.com/`).
 * Authentication is performed via a personal access token set in [token].
 */
interface BitbucketServerClientInfo : ServiceClientInfo<BitbucketServerService> {
    /**
     * The base URL of the Bitbucket Data Center instance (e.g. `https://bitbucket.example.com/`).
     */
    val baseUrl: Property<String>

    /**
     * A personal access token (or HTTP access token) for authenticating with the Bitbucket Data Center API.
     */
    val token: Property<String>
}
