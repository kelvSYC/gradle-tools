package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.CommitStatus
import com.kelvsyc.gradle.bitbucket.cloud.model.PaginatedResponse
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequest
import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequestComment
import com.kelvsyc.gradle.bitbucket.cloud.model.Repository
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Url

/**
 * Retrofit service interface for the Bitbucket Cloud REST API (v2.0).
 *
 * Methods return [Call] to allow both synchronous (via [Call.execute]) and enqueued async usage.
 */
interface BitbucketCloudService {
    /**
     * Retrieves a repository by its workspace and repo slug.
     */
    @GET("repositories/{workspace}/{repo_slug}")
    fun getRepository(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
    ): Call<Repository>

    /**
     * Lists pull requests for a repository.
     */
    @GET("repositories/{workspace}/{repo_slug}/pullrequests")
    fun listPullRequests(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
        @QueryMap options: Map<String, String> = emptyMap(),
    ): Call<PaginatedResponse<PullRequest>>

    /**
     * Fetches the next page of pull requests from a pagination URL.
     */
    @GET
    fun listPullRequestsPage(@Url url: String): Call<PaginatedResponse<PullRequest>>

    /**
     * Retrieves a single pull request by its ID.
     */
    @GET("repositories/{workspace}/{repo_slug}/pullrequests/{pull_request_id}")
    fun getPullRequest(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
        @Path("pull_request_id") pullRequestId: Long,
    ): Call<PullRequest>

    /**
     * Creates or updates a build status (commit status) on a commit.
     */
    @PUT("repositories/{workspace}/{repo_slug}/commit/{commit}/statuses/build/{key}")
    fun putCommitStatus(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
        @Path("commit") commit: String,
        @Path("key") key: String,
        @Body body: Map<String, Any>,
    ): Call<CommitStatus>

    /**
     * Retrieves build statuses for a commit.
     */
    @GET("repositories/{workspace}/{repo_slug}/commit/{commit}/statuses")
    fun getCommitStatuses(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
        @Path("commit") commit: String,
    ): Call<PaginatedResponse<CommitStatus>>

    /**
     * Fetches the next page of commit statuses from a pagination URL.
     */
    @GET
    fun getCommitStatusesPage(@Url url: String): Call<PaginatedResponse<CommitStatus>>

    /**
     * Creates a comment on a pull request.
     */
    @POST("repositories/{workspace}/{repo_slug}/pullrequests/{pull_request_id}/comments")
    fun createPullRequestComment(
        @Path("workspace") workspace: String,
        @Path("repo_slug") repoSlug: String,
        @Path("pull_request_id") pullRequestId: Long,
        @Body body: Map<String, Any>,
    ): Call<PullRequestComment>
}
