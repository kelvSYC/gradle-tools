package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.git.GetGitRemoteArchive
import com.kelvsyc.gradle.git.GitExport
import com.kelvsyc.gradle.git.which
import com.kelvsyc.gradle.github.GetGitHubRepoArchive
import com.kelvsyc.gradle.gitlab.GetGitLabRepoArchive
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitCorePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(GetGitRemoteArchive::class.java).configureEach {
            gitCommand.convention(project.providers.which("git"))
        }
        project.tasks.withType(GitExport::class.java).configureEach {
            gitCommand.convention(project.providers.which("git"))
        }
        project.tasks.withType(GetGitHubRepoArchive::class.java).configureEach {
            ghCommand.convention(project.providers.which("gh"))
        }
        project.tasks.withType(GetGitLabRepoArchive::class.java).configureEach {
            glabCommand.convention(project.providers.which("glab"))
        }
    }
}
