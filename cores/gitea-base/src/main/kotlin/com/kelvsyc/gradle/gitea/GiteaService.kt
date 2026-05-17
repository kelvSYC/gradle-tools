package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.gitea.models.Comment
import com.kelvsyc.gradle.gitea.models.CommitStatus
import com.kelvsyc.gradle.gitea.models.PullRequest
import com.kelvsyc.gradle.gitea.models.Release
import com.kelvsyc.gradle.gitea.models.Repository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GiteaService {
    @GET("api/v1/repos/{owner}/{repo}")
    fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): Call<Repository>

    @GET("api/v1/repos/{owner}/{repo}/pulls")
    fun listPullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Call<List<PullRequest>>

    @GET("api/v1/repos/{owner}/{repo}/pulls/{index}")
    fun getPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("index") index: Long,
    ): Call<PullRequest>

    @POST("api/v1/repos/{owner}/{repo}/issues/{index}/comments")
    fun createComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("index") index: Long,
        @Body body: Map<String, String>,
    ): Call<Comment>

    @GET("api/v1/repos/{owner}/{repo}/statuses/{sha}")
    fun listStatuses(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Call<List<CommitStatus>>

    @POST("api/v1/repos/{owner}/{repo}/statuses/{sha}")
    fun createStatus(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Body body: Map<String, String?>,
    ): Call<CommitStatus>

    @Streaming
    @GET("api/v1/repos/{owner}/{repo}/archive/{filepath}")
    fun getArchive(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("filepath") filepath: String,
    ): Call<ResponseBody>

    @GET("api/v1/repos/{owner}/{repo}/releases/tags/{tag}")
    fun getReleaseByTag(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tag") tag: String,
    ): Call<Release>

    @Streaming
    @GET
    fun downloadAsset(@Url url: String): Call<ResponseBody>
}
