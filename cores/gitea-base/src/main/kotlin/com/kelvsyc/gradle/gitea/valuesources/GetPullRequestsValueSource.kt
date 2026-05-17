package com.kelvsyc.gradle.gitea.valuesources

import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.PullRequest
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * ValueSource that retrieves all pull requests for a Gitea repository.
 *
 * Pages are fetched lazily and collected into a list. All pages are fetched —
 * for partial consumption, extend [AbstractPaginatedValueSource] directly.
 */
abstract class GetPullRequestsValueSource
    : AbstractCollectedPaginatedValueSource<PullRequest, GetPullRequestsValueSource.Parameters>() {

    /**
     * Parameters for [GetPullRequestsValueSource].
     */
    interface Parameters : PaginatedParameters {
        /** Repository owner (username or organization). */
        @get:Input
        val owner: Property<String>

        /** Repository name. */
        @get:Input
        val repo: Property<String>

        /** Filter by state: "open", "closed", or "all". Omit to use the API default. */
        @get:Input
        @get:Optional
        val state: Property<String>
    }

    override fun fetchPage(service: GiteaService, page: Int, limit: Int): List<PullRequest> =
        service.listPullRequests(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            state = parameters.state.orNull,
            page = page,
            limit = limit,
        ).execute().body() ?: emptyList()
}

