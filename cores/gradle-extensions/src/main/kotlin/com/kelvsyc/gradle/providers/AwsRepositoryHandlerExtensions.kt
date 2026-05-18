package com.kelvsyc.gradle.providers

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

/**
 * Adds and configures an AWS CodeArtifact repository.
 *
 * @param name The Gradle name of the repository
 * @param domain The CodeArtifact domain name
 * @param domainOwner The AWS account ID that owns the domain
 * @param region The AWS region
 * @param repo The CodeArtifact repository name
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
 * @param domain The CodeArtifact domain name
 * @param domainOwner The AWS account ID that owns the domain
 * @param region The AWS region
 * @param repo The CodeArtifact repository name
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
