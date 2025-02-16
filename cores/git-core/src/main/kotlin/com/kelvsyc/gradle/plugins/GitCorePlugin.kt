package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.git.GetGitRemoteArchive
import com.kelvsyc.gradle.git.GitExport
import com.kelvsyc.gradle.git.which
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class GitCorePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType<GetGitRemoteArchive>().configureEach {
            gitCommand.convention(project.providers.which("git"))
        }
        project.tasks.withType<GitExport>().configureEach {
            gitCommand.convention(project.providers.which("git"))
        }
    }
}
