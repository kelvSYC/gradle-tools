package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [Storage] client instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring `parameters.projectId` and the credential source via the extension functions on
 * [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount],
 * [accessToken][com.kelvsyc.gradle.google.cloud.accessToken]). The same registration can then be
 * shared with value sources, work actions and tasks via a `Property<StorageClientBuildService>`
 * parameter.
 */
abstract class StorageClientBuildService :
    AbstractGcpClientBuildService<Storage, GcpBuildServiceParams>() {

    override fun createClient(): Storage = StorageOptions.newBuilder().apply {
        parameters.projectId.orNull?.let(::setProjectId)
        resolveCredentials()?.let(::setCredentials)
    }.build().service
}
