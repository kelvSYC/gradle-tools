package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import java.net.URI

class GitRegistryRepositoryHandlerExtensionsSpec : FunSpec() {
    init {
        test("gitHubPackages sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.gitHubPackages("MyRepo", "owner", "repo")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://maven.pkg.github.com/owner/repo")
        }

        test("gitHubPackages with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.gitHubPackages("MyRepo", "owner", "repo") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://maven.pkg.github.com/owner/repo")
            actionInvoked shouldBeEqual true
        }

        test("gitLabPackages sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.gitLabPackages("MyRepo", "12345")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://gitlab.com/api/v4/projects/12345/packages/maven")
        }

        test("gitLabPackages with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.gitLabPackages("MyRepo", "12345") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://gitlab.com/api/v4/projects/12345/packages/maven")
            actionInvoked shouldBeEqual true
        }

        test("giteaPackages sets name and URL") {
            val project = ProjectBuilder.builder().build()

            project.repositories.giteaPackages("MyRepo", "https://gitea.example.com", "myorg")

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://gitea.example.com/api/packages/myorg/maven")
        }

        test("giteaPackages with action sets name, URL, and invokes action") {
            val project = ProjectBuilder.builder().build()
            var actionInvoked = false

            project.repositories.giteaPackages("MyRepo", "https://gitea.example.com", "myorg") {
                actionInvoked = true
            }

            val repo = project.repositories.findByName("MyRepo")
            repo.shouldBeInstanceOf<MavenArtifactRepository>()
            repo.url shouldBeEqual URI.create("https://gitea.example.com/api/packages/myorg/maven")
            actionInvoked shouldBeEqual true
        }
    }
}
