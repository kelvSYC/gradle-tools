package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.git.GetGitRemoteArchive
import com.kelvsyc.gradle.git.GitExport
import com.kelvsyc.gradle.github.GetGitHubRepoArchive
import io.kotest.core.spec.style.FunSpec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class GitCorePluginSpec : FunSpec() {
    init {
        test("Apply - configures GetGitRemoteArchive tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GitCorePlugin::class)

            project.tasks.register<GetGitRemoteArchive>("myTask") {
                remoteUrl.set("https://example.com/repo.git")
                ref.set("main")
                outputFile.set(project.layout.buildDirectory.file("archive.zip"))
            }.get()
        }

        test("Apply - configures GitExport tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GitCorePlugin::class)

            project.tasks.register<GitExport>("myTask") {
                remoteUrl.set("https://example.com/repo.git")
                ref.set("main")
                outputDirectory.set(project.layout.buildDirectory.dir("export"))
            }.get()
        }

        test("Apply - configures GetGitHubRepoArchive tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GitCorePlugin::class)

            project.tasks.register<GetGitHubRepoArchive>("myTask") {
                owner.set("myowner")
                repo.set("myrepo")
                ref.set("main")
                outputFile.set(project.layout.buildDirectory.file("archive.tar.gz"))
            }.get()
        }
    }
}
