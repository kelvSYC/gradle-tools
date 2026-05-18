package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

/**
 * Adds and configures a GCP Artifact Registry Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param region The GCP region (e.g. `us-central1`)
 * @param project The GCP project ID
 * @param repo The Artifact Registry repository name
 */
fun RepositoryHandler.gcpArtifactRegistry(
    name: String,
    region: String,
    project: String,
    repo: String
) = mavenKt {
    this.name = name
    url = URI.create("https://$region-maven.pkg.dev/$project/$repo/")
}

/**
 * Adds and configures a GCP Artifact Registry Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param region The GCP region (e.g. `us-central1`)
 * @param project The GCP project ID
 * @param repo The Artifact Registry repository name
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.gcpArtifactRegistry(
    name: String,
    region: String,
    project: String,
    repo: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("https://$region-maven.pkg.dev/$project/$repo/")
    action(this)
}
