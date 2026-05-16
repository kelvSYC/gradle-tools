package com.kelvsyc.gradle.nexus

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.clients.CredentialReference
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Build service managing a [NexusService] Retrofit client for a Sonatype Nexus Repository Manager 3 instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.baseUrl] and optionally [Params.username] and [Params.passwordRef] via the [anonymous] or
 * [basicAuth] extension functions. The same registration can then be shared with value sources and
 * work actions via a `Property<NexusClientBuildService>` parameter.
 */
abstract class NexusClientBuildService :
    AbstractClientBuildService<NexusService, NexusClientBuildService.Params>() {

    /**
     * Configuration parameters for [NexusClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The base URL of the Nexus Repository Manager instance (e.g. `https://nexus.example.com/`).
         * Must include a trailing slash.
         */
        val baseUrl: Property<String>

        /**
         * The Nexus username for basic authentication. Leave unset for anonymous access.
         */
        val username: Property<String>

        /**
         * Reference to where the Nexus password can be found. Ignored when [username] is absent.
         *
         * Stores a [CredentialReference] pointing to an environment variable or system property
         * whose value is the password. Set via the [basicAuth] extension function.
         */
        val passwordRef: Property<CredentialReference>
    }

    override fun createClient(): NexusService {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val httpClientBuilder = OkHttpClient.Builder()

        if (parameters.username.isPresent) {
            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "Authorization",
                        Credentials.basic(
                            parameters.username.get(),
                            parameters.passwordRef.orNull?.resolve() ?: "",
                        ),
                    )
                    .build()
                chain.proceed(request)
            }
            httpClientBuilder.addInterceptor(authInterceptor)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(parameters.baseUrl.get())
            .client(httpClientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(NexusService::class.java)
    }
}
