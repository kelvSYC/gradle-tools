package com.kelvsyc.gradle.internal.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientInfo
import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudService
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Internal implementation of [BitbucketCloudClientInfo] that constructs a [BitbucketCloudService] Retrofit client.
 */
abstract class BitbucketCloudClientInfoInternal : BitbucketCloudClientInfo,
    ServiceClientInfoInternal<BitbucketCloudService> {
    override fun createClient(): BitbucketCloudService {
        val creds = credentials.get()
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", Credentials.basic(creds.username.orEmpty(), creds.password.orEmpty()))
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
            .baseUrl(baseUrl.getOrElse(DEFAULT_BASE_URL))
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(BitbucketCloudService::class.java)
    }

    private companion object {
        private const val DEFAULT_BASE_URL = "https://api.bitbucket.org/2.0/"
    }
}
