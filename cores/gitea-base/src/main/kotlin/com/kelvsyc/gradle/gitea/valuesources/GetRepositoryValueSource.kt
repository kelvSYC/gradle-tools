package com.kelvsyc.gradle.gitea.valuesources

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import com.kelvsyc.gradle.gitea.models.Repository
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * ValueSource that fetches a single [Repository] from the Gitea API.
 *
 * Returns null if the repository is not found or the request fails.
 */
abstract class GetRepositoryValueSource : ValueSource<Repository, GetRepositoryValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryValueSource].
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
    }

    override fun obtain(): Repository? =
        parameters.service.get().getClient()
            .getRepository(parameters.owner.get(), parameters.repo.get())
            .execute().body()
}

