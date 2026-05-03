package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.jfrog.AddGitInfoToBuild
import com.kelvsyc.gradle.jfrog.CleanBuildInfo
import com.kelvsyc.gradle.jfrog.CollectBuildEnvironment
import com.kelvsyc.gradle.jfrog.PublishBuildInfo
import com.kelvsyc.gradle.jfrog.ScanBuild
import io.kotest.core.spec.style.FunSpec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class JFrogCliPluginSpec : FunSpec() {
    init {
        test("Apply - configures AddGitInfoToBuild tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(JFrogCliPlugin::class)

            project.tasks.register<AddGitInfoToBuild>("myTask") {
                buildName.set("my-build")
                buildNumber.set("1")
            }.get()
        }

        test("Apply - configures CollectBuildEnvironment tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(JFrogCliPlugin::class)

            project.tasks.register<CollectBuildEnvironment>("myTask") {
                buildName.set("my-build")
                buildNumber.set("1")
            }.get()
        }

        test("Apply - configures PublishBuildInfo tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(JFrogCliPlugin::class)

            project.tasks.register<PublishBuildInfo>("myTask") {
                serverUrl.set("https://artifactory.example.com")
                accessToken.set("mytoken")
                buildName.set("my-build")
                buildNumber.set("1")
            }.get()
        }

        test("Apply - configures CleanBuildInfo tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(JFrogCliPlugin::class)

            project.tasks.register<CleanBuildInfo>("myTask") {
                buildName.set("my-build")
                buildNumber.set("1")
            }.get()
        }

        test("Apply - configures ScanBuild tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(JFrogCliPlugin::class)

            project.tasks.register<ScanBuild>("myTask") {
                serverUrl.set("https://artifactory.example.com")
                accessToken.set("mytoken")
                buildName.set("my-build")
                buildNumber.set("1")
            }.get()
        }
    }
}
