package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import java.net.URI

class ArtifactoryRepositoryHandlerExtensionsSpec : FunSpec() {
    init {
        test("artifactoryMaven sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.artifactoryMaven("MyRepo", "https://artifactory.example.com/artifactory", "libs-release")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://artifactory.example.com/artifactory/libs-release")
        }

        test("artifactoryMaven with trailing slash in serverUrl sets correct URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.artifactoryMaven("MyRepo", "https://artifactory.example.com/artifactory/", "libs-release")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://artifactory.example.com/artifactory/libs-release")
        }

        test("artifactoryMaven with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.artifactoryMaven("MyRepo", "https://artifactory.example.com/artifactory", "libs-release") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://artifactory.example.com/artifactory/libs-release")
            actionInvoked shouldBeEqual true
        }

        test("artifactoryIvy sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.artifactoryIvy("MyRepo", "https://artifactory.example.com/artifactory", "libs-ivy")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<IvyArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://artifactory.example.com/artifactory/libs-ivy")
        }

        test("artifactoryIvy with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.artifactoryIvy("MyRepo", "https://artifactory.example.com/artifactory", "libs-ivy") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<IvyArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://artifactory.example.com/artifactory/libs-ivy")
            actionInvoked shouldBeEqual true
        }

        test("jfrogCloudMaven sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.jfrogCloudMaven("MyRepo", "mycompany", "libs-release")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://mycompany.jfrog.io/artifactory/libs-release")
        }

        test("jfrogCloudMaven with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.jfrogCloudMaven("MyRepo", "mycompany", "libs-release") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://mycompany.jfrog.io/artifactory/libs-release")
            actionInvoked shouldBeEqual true
        }

        test("jfrogCloudIvy sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.jfrogCloudIvy("MyRepo", "mycompany", "libs-ivy")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<IvyArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://mycompany.jfrog.io/artifactory/libs-ivy")
        }

        test("jfrogCloudIvy with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.jfrogCloudIvy("MyRepo", "mycompany", "libs-ivy") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<IvyArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://mycompany.jfrog.io/artifactory/libs-ivy")
            actionInvoked shouldBeEqual true
        }
    }
}
