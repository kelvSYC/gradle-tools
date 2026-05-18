package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

// Internal workarounds since Gradle libraries do not treat Action<in T> as T.() -> Unit
internal fun RepositoryHandler.mavenKt(action: MavenArtifactRepository.() -> Unit) = maven(action)
internal fun RepositoryHandler.ivyKt(action: IvyArtifactRepository.() -> Unit) = ivy(action)

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
 * Adds and configures a Gitea or Forgejo Maven package registry repository.
 *
 * @param name The Gradle name of the repository
 * @param baseUrl The Gitea/Forgejo server root URL (e.g. `https://gitea.example.com`), without a trailing slash
 * @param owner The Gitea/Forgejo user or organisation that owns the package namespace
 */
fun RepositoryHandler.giteaPackages(name: String, baseUrl: String, owner: String) = mavenKt {
    this.name = name
    url = URI.create("$baseUrl/api/packages/$owner/maven")
}

/**
 * Adds and configures a Gitea or Forgejo Maven package registry repository.
 *
 * @param name The Gradle name of the repository
 * @param baseUrl The Gitea/Forgejo server root URL (e.g. `https://gitea.example.com`), without a trailing slash
 * @param owner The Gitea/Forgejo user or organisation that owns the package namespace
 * @param action Configuration action for further configuration of the artifact repository
 */
fun RepositoryHandler.giteaPackages(
    name: String,
    baseUrl: String,
    owner: String,
    action: MavenArtifactRepository.() -> Unit
) = mavenKt {
    this.name = name
    url = URI.create("$baseUrl/api/packages/$owner/maven")
    action(this)
}
