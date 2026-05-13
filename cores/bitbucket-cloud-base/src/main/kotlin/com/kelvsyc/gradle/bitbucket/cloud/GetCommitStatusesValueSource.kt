package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.CommitStatus
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches all build statuses for a commit from the Bitbucket Cloud API.
 *
 * Automatically follows pagination to collect statuses from all pages.
 */
abstract class GetCommitStatusesValueSource :
    ValueSource<List<CommitStatus>, GetCommitStatusesValueSource.Parameters> {
    /**
     * Parameters for [GetCommitStatusesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Bitbucket Cloud client. */
        @get:Internal
        val service: Property<BitbucketCloudClientBuildService>

        /** The Bitbucket workspace slug or UUID. */
        val workspace: Property<String>

        /** The repository slug. */
        val repoSlug: Property<String>

        /** The full commit hash. */
        val commit: Property<String>
    }

    override fun obtain(): List<CommitStatus> {
        val service = parameters.service.get().getClient()
        return fetchAllPages(
            firstPage = service.getCommitStatuses(
                workspace = parameters.workspace.get(),
                repoSlug = parameters.repoSlug.get(),
                commit = parameters.commit.get(),
            ),
            nextPage = service::getCommitStatusesPage,
        )
    }
}
