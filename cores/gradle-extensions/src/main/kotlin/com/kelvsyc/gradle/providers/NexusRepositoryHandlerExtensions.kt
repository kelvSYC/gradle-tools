package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

/**
 * Adds and configures a Sonatype Nexus Repository Manager 3 Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param baseUrl The Nexus server URL (e.g. `https://nexus.example.com`), with or without a trailing slash
 * @param repoName The name of the Nexus Maven repository
 */
fun RepositoryHandler.nexusMaven(name: String, baseUrl: String, repoName: String) = mavenKt {
    this.name = name
    url = URI.create("${baseUrl.trimEnd('/')}/repository/$repoName")
}

/**
 * Adds and configures a Sonatype Nexus Repository Manager 3 Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param baseUrl The Nexus server URL (e.g. `https://nexus.example.com`), with or without a trailing slash
 * @param repoName The name of the Nexus Maven repository
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.nexusMaven(
    name: String,
    baseUrl: String,
    repoName: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("${baseUrl.trimEnd('/')}/repository/$repoName")
    action(this)
}
