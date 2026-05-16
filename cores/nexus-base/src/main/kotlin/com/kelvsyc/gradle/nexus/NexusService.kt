package com.kelvsyc.gradle.nexus

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface NexusService {
    /**
     * Downloads an artifact from a raw repository by full path, streaming the response body.
     */
    @Streaming
    @GET("repository/{repository}/{path}")
    fun downloadAsset(
        @Path("repository") repository: String,
        @Path("path", encoded = true) path: String,
    ): Call<ResponseBody>

    /**
     * Uploads a file to a raw repository using the Nexus REST v1 multipart format.
     */
    @Multipart
    @POST("service/rest/v1/components")
    fun uploadRawAsset(
        @Query("repository") repository: String,
        @Part("raw.directory") directory: RequestBody,
        @Part("raw.asset1.filename") filename: RequestBody,
        @Part raw: MultipartBody.Part,
    ): Call<ResponseBody>
}
