package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import java.net.URI

class GcpRepositoryHandlerExtensionsSpec : FunSpec() {
    init {
        test("gcpArtifactRegistry sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.gcpArtifactRegistry("MyRepo", "us-central1", "my-project", "my-repo")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://us-central1-maven.pkg.dev/my-project/my-repo/")
        }

        test("gcpArtifactRegistry with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.gcpArtifactRegistry("MyRepo", "us-central1", "my-project", "my-repo") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://us-central1-maven.pkg.dev/my-project/my-repo/")
            actionInvoked shouldBeEqual true
        }
    }
}
