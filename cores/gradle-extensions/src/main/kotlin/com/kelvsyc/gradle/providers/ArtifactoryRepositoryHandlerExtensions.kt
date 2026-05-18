package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

/**
 * Adds and configures a self-hosted JFrog Artifactory Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param serverUrl The Artifactory server URL including the context path
 *   (e.g. `https://artifactory.example.com/artifactory`), with or without a trailing slash
 * @param repoKey The Artifactory repository key
 */
fun RepositoryHandler.artifactoryMaven(name: String, serverUrl: String, repoKey: String) = mavenKt {
    this.name = name
    url = URI.create("${serverUrl.trimEnd('/')}/$repoKey")
}

/**
 * Adds and configures a self-hosted JFrog Artifactory Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param serverUrl The Artifactory server URL including the context path
 *   (e.g. `https://artifactory.example.com/artifactory`), with or without a trailing slash
 * @param repoKey The Artifactory repository key
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.artifactoryMaven(
    name: String,
    serverUrl: String,
    repoKey: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("${serverUrl.trimEnd('/')}/$repoKey")
    action(this)
}

/**
 * Adds and configures a self-hosted JFrog Artifactory Ivy repository.
 *
 * @param name The Gradle name of the repository
 * @param serverUrl The Artifactory server URL including the context path
 *   (e.g. `https://artifactory.example.com/artifactory`), with or without a trailing slash
 * @param repoKey The Artifactory repository key
 */
fun RepositoryHandler.artifactoryIvy(name: String, serverUrl: String, repoKey: String) = ivyKt {
    this.name = name
    url = URI.create("${serverUrl.trimEnd('/')}/$repoKey")
}

/**
 * Adds and configures a self-hosted JFrog Artifactory Ivy repository.
 *
 * @param name The Gradle name of the repository
 * @param serverUrl The Artifactory server URL including the context path
 *   (e.g. `https://artifactory.example.com/artifactory`), with or without a trailing slash
 * @param repoKey The Artifactory repository key
 * @param action Configuration action for further configuration of the artifact repository, including layout patterns
 */
fun RepositoryHandler.artifactoryIvy(
    name: String,
    serverUrl: String,
    repoKey: String,
    action: IvyArtifactRepository.() -> Unit
) = ivyKt {
    this.name = name
    url = URI.create("${serverUrl.trimEnd('/')}/$repoKey")
    action(this)
}

/**
 * Adds and configures a JFrog Cloud Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param orgName The JFrog Cloud organisation name (the subdomain of `jfrog.io`)
 * @param repoKey The Artifactory repository key
 */
fun RepositoryHandler.jfrogCloudMaven(name: String, orgName: String, repoKey: String) = mavenKt {
    this.name = name
    url = URI.create("https://$orgName.jfrog.io/artifactory/$repoKey")
}

/**
 * Adds and configures a JFrog Cloud Maven repository.
 *
 * @param name The Gradle name of the repository
 * @param orgName The JFrog Cloud organisation name (the subdomain of `jfrog.io`)
 * @param repoKey The Artifactory repository key
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.jfrogCloudMaven(
    name: String,
    orgName: String,
    repoKey: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("https://$orgName.jfrog.io/artifactory/$repoKey")
    action(this)
}

/**
 * Adds and configures a JFrog Cloud Ivy repository.
 *
 * @param name The Gradle name of the repository
 * @param orgName The JFrog Cloud organisation name (the subdomain of `jfrog.io`)
 * @param repoKey The Artifactory repository key
 */
fun RepositoryHandler.jfrogCloudIvy(name: String, orgName: String, repoKey: String) = ivyKt {
    this.name = name
    url = URI.create("https://$orgName.jfrog.io/artifactory/$repoKey")
}

/**
 * Adds and configures a JFrog Cloud Ivy repository.
 *
 * @param name The Gradle name of the repository
 * @param orgName The JFrog Cloud organisation name (the subdomain of `jfrog.io`)
 * @param repoKey The Artifactory repository key
 * @param action Configuration action for further configuration of the artifact repository, including layout patterns
 */
fun RepositoryHandler.jfrogCloudIvy(
    name: String,
    orgName: String,
    repoKey: String,
    action: IvyArtifactRepository.() -> Unit
) = ivyKt {
    this.name = name
    url = URI.create("https://$orgName.jfrog.io/artifactory/$repoKey")
    action(this)
}
