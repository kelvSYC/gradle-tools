package com.kelvsyc.gradle.gitea

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal fun buildGiteaService(baseUrl: String, httpClient: OkHttpClient): GiteaService {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GiteaService::class.java)
}
