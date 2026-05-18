package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import java.net.URI

class AwsRepositoryHandlerExtensionsSpec : FunSpec() {
    init {
        test("awsCodeArtifact sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.awsCodeArtifact("MyRepo", "mydomain", "123456789", "us-east-1", "myrepo")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create(
                "https://mydomain-123456789.d.codeartifact.us-east-1.amazonaws.com/maven/myrepo"
            )
        }

        test("awsCodeArtifact with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.awsCodeArtifact("MyRepo", "mydomain", "123456789", "us-east-1", "myrepo") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create(
                "https://mydomain-123456789.d.codeartifact.us-east-1.amazonaws.com/maven/myrepo"
            )
            actionInvoked shouldBeEqual true
        }
    }
}
