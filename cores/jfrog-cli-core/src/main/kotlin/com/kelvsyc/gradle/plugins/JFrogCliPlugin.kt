package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.jfrog.AddGitInfoToBuild
import com.kelvsyc.gradle.jfrog.CleanBuildInfo
import com.kelvsyc.gradle.jfrog.CollectBuildEnvironment
import com.kelvsyc.gradle.jfrog.PublishBuildInfo
import com.kelvsyc.gradle.jfrog.ScanBuild
import com.kelvsyc.gradle.jfrog.which
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

class JFrogCliPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType<AddGitInfoToBuild>().configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType<CollectBuildEnvironment>().configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType<PublishBuildInfo>().configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType<CleanBuildInfo>().configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType<ScanBuild>().configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
    }
}
