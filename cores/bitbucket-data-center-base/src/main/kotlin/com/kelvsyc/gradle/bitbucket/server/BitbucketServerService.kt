package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.BuildStatus
import com.kelvsyc.gradle.bitbucket.server.model.Comment
import com.kelvsyc.gradle.bitbucket.server.model.PaginatedResponse
import com.kelvsyc.gradle.bitbucket.server.model.PullRequest
import com.kelvsyc.gradle.bitbucket.server.model.Repository
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the Bitbucket Data Center REST API.
 *
 * The base URL should be the server root (e.g. `https://bitbucket.example.com/`).
 * Core API endpoints use the `/rest/api/1.0/` prefix; the build status API uses
 * `/rest/build-status/1.0/`.
 */
interface BitbucketServerService {
    /**
     * Retrieves a repository by its project key and repo slug.
     */
    @GET("rest/api/1.0/projects/{projectKey}/repos/{repoSlug}")
    fun getRepository(
        @Path("projectKey") projectKey: String,
        @Path("repoSlug") repoSlug: String,
    ): Call<Repository>

    /**
     * Lists pull requests for a repository.
     */
    @GET("rest/api/1.0/projects/{projectKey}/repos/{repoSlug}/pull-requests")
    fun listPullRequests(
        @Path("projectKey") projectKey: String,
        @Path("repoSlug") repoSlug: String,
        @Query("start") start: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("state") state: String? = null,
    ): Call<PaginatedResponse<PullRequest>>

    /**
     * Retrieves a single pull request by its ID.
     */
    @GET("rest/api/1.0/projects/{projectKey}/repos/{repoSlug}/pull-requests/{pullRequestId}")
    fun getPullRequest(
        @Path("projectKey") projectKey: String,
        @Path("repoSlug") repoSlug: String,
        @Path("pullRequestId") pullRequestId: Long,
    ): Call<PullRequest>

    /**
     * Creates a comment on a pull request.
     */
    @POST("rest/api/1.0/projects/{projectKey}/repos/{repoSlug}/pull-requests/{pullRequestId}/comments")
    fun createPullRequestComment(
        @Path("projectKey") projectKey: String,
        @Path("repoSlug") repoSlug: String,
        @Path("pullRequestId") pullRequestId: Long,
        @Body body: Map<String, Any>,
    ): Call<Comment>

    /**
     * Retrieves build statuses for a commit.
     */
    @GET("rest/build-status/1.0/commits/{commitId}")
    fun getBuildStatuses(
        @Path("commitId") commitId: String,
        @Query("start") start: Int? = null,
        @Query("limit") limit: Int? = null,
    ): Call<PaginatedResponse<BuildStatus>>

    /**
     * Posts a build status to a commit.
     */
    @POST("rest/build-status/1.0/commits/{commitId}")
    fun postBuildStatus(
        @Path("commitId") commitId: String,
        @Body body: Map<String, Any>,
    ): Call<Void>
}
