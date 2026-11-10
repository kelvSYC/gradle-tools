package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.jfrog.AddGitInfoToBuild
import com.kelvsyc.gradle.jfrog.CleanBuildInfo
import com.kelvsyc.gradle.jfrog.CollectBuildEnvironment
import com.kelvsyc.gradle.jfrog.PublishBuildInfo
import com.kelvsyc.gradle.jfrog.ScanBuild
import com.kelvsyc.gradle.jfrog.which
import org.gradle.api.Plugin
import org.gradle.api.Project

class JFrogCliPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(AddGitInfoToBuild::class.java).configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType(CollectBuildEnvironment::class.java).configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType(PublishBuildInfo::class.java).configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType(CleanBuildInfo::class.java).configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
        project.tasks.withType(ScanBuild::class.java).configureEach {
            jfCommand.convention(project.providers.which("jf"))
        }
    }
}
