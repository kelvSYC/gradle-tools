package com.kelvsyc.gradle.gitea.valuesources

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.PullRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * ValueSource that fetches a single [PullRequest] from the Gitea API by index.
 *
 * Returns null if the pull request is not found.
 */
abstract class GetPullRequestValueSource : ValueSource<PullRequest, GetPullRequestValueSource.Parameters> {
    /**
     * Parameters for [GetPullRequestValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The Gitea build service providing the authenticated [GiteaService] client. */
        @get:Internal
        val service: Property<AbstractClientBuildService<GiteaService, *>>

        /** Repository owner (username or organization). */
        @get:Input
        val owner: Property<String>

        /** Repository name. */
        @get:Input
        val repo: Property<String>

        /** Pull request index (sequential ID within the repository). */
        @get:Input
        val index: Property<Long>
    }

    override fun obtain(): PullRequest? =
        parameters.service.get().getClient()
            .getPullRequest(parameters.owner.get(), parameters.repo.get(), parameters.index.get())
            .execute().body()
}

