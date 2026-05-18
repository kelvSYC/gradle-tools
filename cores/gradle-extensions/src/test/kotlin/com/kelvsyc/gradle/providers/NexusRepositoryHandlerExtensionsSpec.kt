package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import java.net.URI

class NexusRepositoryHandlerExtensionsSpec : FunSpec() {
    init {
        test("nexusMaven sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.nexusMaven("MyRepo", "https://nexus.example.com", "releases")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://nexus.example.com/repository/releases")
        }

        test("nexusMaven with trailing slash in baseUrl sets correct URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.nexusMaven("MyRepo", "https://nexus.example.com/", "releases")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://nexus.example.com/repository/releases")
        }

        test("nexusMaven with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.nexusMaven("MyRepo", "https://nexus.example.com", "releases") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://nexus.example.com/repository/releases")
            actionInvoked shouldBeEqual true
        }
    }
}
