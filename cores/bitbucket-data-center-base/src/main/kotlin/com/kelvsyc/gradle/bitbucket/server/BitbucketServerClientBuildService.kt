package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Build service managing a [BitbucketServerService] Retrofit client.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.baseUrl] and [Params.token]. The same registration can then be shared with value sources and
 * work actions via a `Property<BitbucketServerClientBuildService>` parameter.
 */
abstract class BitbucketServerClientBuildService :
    AbstractClientBuildService<BitbucketServerService, BitbucketServerClientBuildService.Params>() {
    /**
     * Configuration parameters for [BitbucketServerClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The base URL of the Bitbucket Data Center instance (e.g. `https://bitbucket.example.com/`).
         */
        val baseUrl: Property<String>

        /**
         * A personal access token (or HTTP access token) for authenticating with the Bitbucket Data Center API.
         */
        val token: Property<String>
    }

    override fun createClient(): BitbucketServerService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${parameters.token.get()}")
                .build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(parameters.baseUrl.get())
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(BitbucketServerService::class.java)
    }
}
