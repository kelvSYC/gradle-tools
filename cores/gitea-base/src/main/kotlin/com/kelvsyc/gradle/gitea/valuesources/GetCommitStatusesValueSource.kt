package com.kelvsyc.gradle.gitea.valuesources

import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.CommitStatus
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * ValueSource that retrieves all commit statuses for a given SHA in a Gitea repository.
 *
 * Pages are fetched lazily and collected into a list.
 */
abstract class GetCommitStatusesValueSource
    : AbstractCollectedPaginatedValueSource<CommitStatus, GetCommitStatusesValueSource.Parameters>() {

    /**
     * Parameters for [GetCommitStatusesValueSource].
     */
    interface Parameters : PaginatedParameters {
        /** Repository owner (username or organization). */
        @get:Input
        val owner: Property<String>

        /** Repository name. */
        @get:Input
        val repo: Property<String>

        /** Commit SHA. */
        @get:Input
        val sha: Property<String>
    }

    override fun fetchPage(service: GiteaService, page: Int, limit: Int): List<CommitStatus> =
        service.listStatuses(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            sha = parameters.sha.get(),
            page = page,
            limit = limit,
        ).execute().body() ?: emptyList()
}

