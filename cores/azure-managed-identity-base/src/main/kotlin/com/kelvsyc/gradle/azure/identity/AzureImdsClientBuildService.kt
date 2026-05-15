package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Build service managing an [AzureImdsService] Retrofit client.
 *
 * The client targets the Azure Instance Metadata Service (IMDS) at `http://169.254.169.254/metadata/`
 * and automatically adds the `Metadata: true` header required by all IMDS endpoints.
 *
 * No user-configurable parameters are needed — the IMDS endpoint is fixed per the Azure specification.
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent]:
 *
 * ```kotlin
 * val imds = gradle.sharedServices.registerIfAbsent("imds", AzureImdsClientBuildService::class) {}
 * ```
 */
abstract class AzureImdsClientBuildService :
    AbstractClientBuildService<AzureImdsService, BuildServiceParameters.None>() {

    override fun createClient(): AzureImdsService {
        val metadataInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Metadata", "true")
                .build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(metadataInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(AzureImdsService::class.java)
    }

    private companion object {
        private const val BASE_URL = "http://169.254.169.254/metadata/"
    }
}
