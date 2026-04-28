package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.dokka.GenerateDashDocset
import org.gradle.api.Plugin
import org.gradle.api.Project

class DokkaDashPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Two-configuration split follows the Gradle 8.3+ idiom:
        //   dependencyScope  — where dependencies are declared (user-overridable via the
        //                      dependencies {} block under the name "dokkaDashWorkerClasspath")
        //   resolvable       — internal; extends the scope and is resolved into actual files
        val workerDeps = project.configurations.dependencyScope("dokkaDashWorkerClasspath") {
            defaultDependencies {
                add(project.dependencies.create("org.xerial:sqlite-jdbc:$SQLITE_JDBC_VERSION"))
            }
        }

        val workerConfig = project.configurations.resolvable("dokkaDashWorkerClasspathResolvable") {
            extendsFrom(workerDeps.get())
        }

        project.tasks.withType(GenerateDashDocset::class.java).configureEach {
            workerClasspath.from(workerConfig)
        }
    }

    companion object {
        const val SQLITE_JDBC_VERSION = "3.47.1.0"
    }
}
