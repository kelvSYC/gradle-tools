package com.kelvsyc.gradle.aws.java.sns.fixtures

import com.kelvsyc.gradle.aws.java.sns.SnsClientBuildService
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Variant A baseline: a `WorkAction` whose `WorkParameters` exposes the SNS BuildService through
 * `Property<SnsClientBuildService>`. This mirrors the production shape (e.g. `PublishAction`) and is the
 * expected-to-succeed counterpart to [ByoClientWorkAction].
 */
abstract class BuildServiceWorkAction : WorkAction<BuildServiceWorkAction.Parameters> {
    /** Parameters for [BuildServiceWorkAction]. */
    interface Parameters : WorkParameters {
        /** The BuildService reference under probe. */
        val service: Property<SnsClientBuildService>
    }

    override fun execute() {
        val s = parameters.service.get()
        check(s::class.qualifiedName != null) { "service class name unexpectedly null" }
    }
}
