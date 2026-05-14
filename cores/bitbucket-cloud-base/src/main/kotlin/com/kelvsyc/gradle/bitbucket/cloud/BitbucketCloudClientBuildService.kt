package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.clients.AbstractClientBuildService
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
 * Build service managing a [BitbucketCloudService] Retrofit client.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.username] and [Params.password] (app password), and optionally [Params.baseUrl]. The same
 * registration can then be shared with value sources and work actions via a
 * `Property<BitbucketCloudClientBuildService>` parameter.
 */
abstract class BitbucketCloudClientBuildService :
    AbstractClientBuildService<BitbucketCloudService, BitbucketCloudClientBuildService.Params>() {
    /**
     * Configuration parameters for [BitbucketCloudClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The base URL for the Bitbucket Cloud REST API. Defaults to `https://api.bitbucket.org/2.0/`.
         */
        val baseUrl: Property<String>

        /**
         * Bitbucket Cloud username for authenticating with the API.
         */
        val username: Property<String>

        /**
         * Bitbucket Cloud app password for authenticating with the API.
         */
        val password: Property<String>
    }

    override fun createClient(): BitbucketCloudService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", Credentials.basic(
                    parameters.username.getOrElse(""),
                    parameters.password.getOrElse(""),
                ))
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
            .baseUrl(parameters.baseUrl.getOrElse(DEFAULT_BASE_URL))
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(BitbucketCloudService::class.java)
    }

    private companion object {
        private const val DEFAULT_BASE_URL = "https://api.bitbucket.org/2.0/"
    }
}
