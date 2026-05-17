package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.clients.CredentialReference
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [GiteaService] Retrofit client authenticated with basic auth.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.baseUrl], [Params.username], and [Params.passwordRef]. The same registration can then be shared
 * with value sources and work actions via a `Property<GiteaBasicClientBuildService>` parameter.
 */
abstract class GiteaBasicClientBuildService :
    AbstractClientBuildService<GiteaService, GiteaBasicClientBuildService.Params>() {
    /**
     * Configuration parameters for [GiteaBasicClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The base URL of the Gitea or Forgejo instance (e.g. `https://gitea.example.com/`).
         */
        val baseUrl: Property<String>

        /**
         * The username for HTTP basic authentication.
         */
        val username: Property<String>

        /**
         * Reference to where the password can be found.
         *
         * Stores a [CredentialReference] pointing to an environment variable or system property
         * whose value is the password for the user. Set via the [basicAuth] extension function.
         */
        val passwordRef: Property<CredentialReference>
    }

    override fun createClient(): GiteaService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", Credentials.basic(
                    parameters.username.getOrElse(""),
                    parameters.passwordRef.orNull?.resolve() ?: "",
                ))
                .build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return buildGiteaService(parameters.baseUrl.get(), httpClient)
    }
}
