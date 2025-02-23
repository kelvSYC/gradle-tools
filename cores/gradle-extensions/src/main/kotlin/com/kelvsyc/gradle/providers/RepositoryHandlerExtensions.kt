package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

// Internal workaround since Gradle libraries do not treat Action<in T> as T.() -> Unit
internal fun RepositoryHandler.mavenKt(action: MavenArtifactRepository.() -> Unit) = maven(action)

/**
 * Adds and configures a GitHub Packages repository.
 *
 * @param name The Gradle name of the repository
 * @param owner The GitHub repository owner
 * @param repository The GitHub repository name
 */
fun RepositoryHandler.gitHubPackages(name: String, owner: String, repository: String) = mavenKt {
    this.name = name
    url = URI.create("https://maven.pkg.github.com/$owner/$repository")
}

/**
 * Adds and configures a GitHub Packages repository.
 *
 * @param name The Gradle name of the repository
 * @param owner The GitHub repository owner
 * @param repository The GitHub repository name
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.gitHubPackages(
    name: String,
    owner: String,
    repository: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("https://maven.pkg.github.com/$owner/$repository")
    action(this)
}

/**
 * Adds and configures a GitLab Package Registry repository.
 *
 * @param name The Gradle name of the repository
 * @param projectId The GitLab project ID
 */
fun RepositoryHandler.gitLabPackages(name: String, projectId: String) = mavenKt {
    this.name = name
    url = URI.create("https://gitlab.com/api/v4/projects/$projectId/packages/maven")
}

/**
 * Adds and configures a GitLab Package Registry repository.
 *
 * @param name The Gradle name of the repository
 * @param projectId The GitLab project ID
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.gitLabPackages(
    name: String,
    projectId: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("https://gitlab.com/api/v4/projects/$projectId/packages/maven")
    action(this)
}

/**
 * Adds and configures an AWS CodeArtifact repository.
 *
 * @param name The Gradle name of the repository
 * @param domain The AWS domain
 * @param domainOwner The ID of the AWS domain owner
 * @param region The AWS region
 * @param repo The repo name
 */
fun RepositoryHandler.awsCodeArtifact(
    name: String,
    domain: String,
    domainOwner: String,
    region: String,
    repo: String
) = mavenKt {
    this.name = name
    url = URI.create("https://$domain-$domainOwner.d.codeartifact.$region.amazonaws.com/maven/$repo")
}

/**
 * Adds and configures an AWS CodeArtifact repository.
 *
 * @param name The Gradle name of the repository
 * @param domain The AWS domain
 * @param domainOwner The ID of the AWS domain owner
 * @param region The AWS region
 * @param repo The repo name
 * @param action Configuration action for further configuration of the artifact repository
 */
@Suppress("detekt:LongParameterList")
fun RepositoryHandler.awsCodeArtifact(
    name: String,
    domain: String,
    domainOwner: String,
    region: String,
    repo: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("https://$domain-$domainOwner.d.codeartifact.$region.amazonaws.com/maven/$repo")
    action(this)
}
