package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.dokka.GenerateDashDocset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class DokkaDashPluginSpec : FunSpec() {
    init {
        test("Apply - plugin applies without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(DokkaDashPlugin::class)
        }

        test("Apply - dokkaDashWorkerClasspath dependency scope is created") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(DokkaDashPlugin::class)

            project.configurations.findByName("dokkaDashWorkerClasspath").shouldNotBeNull()
        }

        test("Apply - dokkaDashWorkerClasspathResolvable configuration is created") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(DokkaDashPlugin::class)

            project.configurations.findByName("dokkaDashWorkerClasspathResolvable").shouldNotBeNull()
        }

        test("Apply - configures GenerateDashDocset tasks without error") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(DokkaDashPlugin::class)

            project.tasks.register<GenerateDashDocset>("myTask") {
                dokkaOutputDirectory.set(project.layout.buildDirectory.dir("dokka"))
                docsetName.set("MyLibrary")
                bundleIdentifier.set("com.example.mylibrary")
                outputDirectory.set(project.layout.buildDirectory.dir("dash"))
            }.get()
        }
    }
}
