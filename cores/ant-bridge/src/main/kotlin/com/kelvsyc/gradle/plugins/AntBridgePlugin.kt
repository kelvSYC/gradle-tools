package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.ant.AntBridgeExtension
import com.kelvsyc.gradle.ant.AntIsolation
import com.kelvsyc.gradle.ant.AntWork
import com.kelvsyc.gradle.ant.internal.AntRuntimeService
import com.kelvsyc.gradle.ant.tasks.AbstractAntTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Bridges legacy Ant usage into configuration-cache-safe Gradle builds.
 *
 * Applying it creates the `antBridge` dependency scope (Ant and its antlibs), the `antBridgeClasspath` resolvable
 * configuration that extends it, the `antBridge` extension and the `antBridgeRuntime` shared build service, which
 * caches the isolated Ant class loaders used to load properties. Every [AntWork] and typed Ant task ([AbstractAntTask]) gets `antBridgeClasspath` as its
 * Ant classpath and class-loader isolation by convention.
 */
class AntBridgePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val scope = project.configurations.dependencyScope(SCOPE_NAME) {
            // Not defaultDependencies: those vanish as soon as a consumer adds an antlib. A preferred version keeps
            // Ant present and lets a consumer override it with strictly(...).
            withDependencies {
                add(project.dependencies.create("org.apache.ant:ant").preferred())
                add(project.dependencies.create("org.apache.ant:ant-junit").preferred())
            }
        }
        val classpath = project.configurations.resolvable(CLASSPATH_NAME) {
            extendsFrom(scope.get())
        }
        val runtime = project.gradle.sharedServices.registerIfAbsent(RUNTIME_SERVICE_NAME, AntRuntimeService::class.java) {}
        val extension = project.extensions.create(EXTENSION_NAME, AntBridgeExtension::class.java, classpath.get(), runtime)
        project.tasks.withType(AntWork::class.java).configureEach {
            antClasspath.from(extension.classpath)
            isolation.convention(AntIsolation.CLASSLOADER)
        }
        project.tasks.withType(AbstractAntTask::class.java).configureEach {
            antClasspath.from(extension.classpath)
            isolation.convention(AntIsolation.CLASSLOADER)
        }
    }

    private fun org.gradle.api.artifacts.Dependency.preferred(): org.gradle.api.artifacts.Dependency {
        (this as org.gradle.api.artifacts.ExternalModuleDependency).version { prefer(DEFAULT_ANT_VERSION) }
        return this
    }

    companion object {
        /** The Ant version added to the `antBridge` configuration as a preference; equals the catalog's `ant`. */
        const val DEFAULT_ANT_VERSION: String = "1.10.18"

        /** Name of the dependency-scope configuration where Ant and antlibs are declared. */
        const val SCOPE_NAME: String = "antBridge"

        /** Name of the resolvable configuration holding the resolved Ant classpath. */
        const val CLASSPATH_NAME: String = "antBridgeClasspath"

        /** Name of the shared build service caching the isolated Ant class loaders. */
        const val RUNTIME_SERVICE_NAME: String = "antBridgeRuntime"

        /** Name of the extension. */
        const val EXTENSION_NAME: String = "antBridge"
    }
}
