package com.kelvsyc.gradle.internal.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.BitbucketServerClientInfo
import com.kelvsyc.gradle.bitbucket.server.BitbucketServerService
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Internal implementation of [BitbucketServerClientInfo] that constructs a [BitbucketServerService] Retrofit client.
 */
abstract class BitbucketServerClientInfoInternal : BitbucketServerClientInfo,
    ServiceClientInfoInternal<BitbucketServerService> {
    override fun createClient(): BitbucketServerService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${token.get()}")
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
            .baseUrl(baseUrl.get())
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(BitbucketServerService::class.java)
    }
}
