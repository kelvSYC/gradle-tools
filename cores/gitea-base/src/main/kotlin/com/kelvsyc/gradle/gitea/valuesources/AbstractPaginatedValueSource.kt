package com.kelvsyc.gradle.gitea.valuesources

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Abstract base for paginated Gitea API ValueSources.
 *
 * [obtain] builds a lazy [Sequence] that fetches pages from the Gitea API sequentially,
 * stopping as soon as a partial page is returned. The sequence is passed to [transform],
 * which subclasses override to consume exactly as many pages as needed — enabling early
 * termination without fetching all pages upfront.
 *
 * [R] must be serializable for Gradle configuration cache compatibility.
 */
abstract class AbstractPaginatedValueSource<T : Any, R : Any, P : AbstractPaginatedValueSource.PaginatedParameters>
    : ValueSource<R, P> {

    /**
     * Parameters common to all paginated ValueSources. Subclasses extend this interface
     * to add endpoint-specific inputs (owner, repo, etc.).
     */
    interface PaginatedParameters : ValueSourceParameters {
        /** The Gitea build service providing the authenticated [GiteaService] client. */
        @get:Internal
        val service: Property<AbstractClientBuildService<GiteaService, *>>
    }

    private companion object {
        private const val PAGE_SIZE = 50
    }

    /**
     * Fetches a single page of results from the Gitea API.
     *
     * Implementations call the appropriate [GiteaService] method with the given [page]
     * and [limit] and return the response body, or an empty list on null response.
     */
    protected abstract fun fetchPage(service: GiteaService, page: Int, limit: Int): List<T>

    /**
     * Transforms the lazy page sequence into the final result [R].
     *
     * The sequence fetches pages on demand — operations like [Sequence.firstOrNull] or
     * [Sequence.filter] followed by [Sequence.first] will stop fetching after the
     * matching item is found, without downloading all remaining pages.
     */
    protected abstract fun transform(sequence: Sequence<T>): R

    override fun obtain(): R {
        val client = parameters.service.get().getClient()
        val sequence = sequence {
            var page = 1
            while (true) {
                val items = fetchPage(client, page, PAGE_SIZE)
                yieldAll(items)
                if (items.size < PAGE_SIZE) break
                page++
            }
        }
        return transform(sequence)
    }
}
