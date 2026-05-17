package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.clients.CredentialReference
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [GiteaService] Retrofit client authenticated with a bearer token.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.baseUrl] and [Params.tokenRef]. The same registration can then be shared with value sources and
 * work actions via a `Property<GiteaBearerClientBuildService>` parameter.
 */
abstract class GiteaBearerClientBuildService :
    AbstractClientBuildService<GiteaService, GiteaBearerClientBuildService.Params>() {
    /**
     * Configuration parameters for [GiteaBearerClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The base URL of the Gitea or Forgejo instance (e.g. `https://gitea.example.com/`).
         */
        val baseUrl: Property<String>

        /**
         * Reference to where the access token can be found.
         *
         * Stores a [CredentialReference] pointing to an environment variable or system property
         * whose value is the Gitea API token. Set via the [bearerToken] extension function.
         */
        val tokenRef: Property<CredentialReference>
    }

    override fun createClient(): GiteaService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "token ${parameters.tokenRef.get().resolve()}")
                .build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return buildGiteaService(parameters.baseUrl.get(), httpClient)
    }
}
