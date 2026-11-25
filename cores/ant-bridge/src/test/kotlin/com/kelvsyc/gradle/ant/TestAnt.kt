package com.kelvsyc.gradle.ant

import org.gradle.api.Project
import java.io.File

/** Ant classpaths for tests, resolved by the build and passed as system properties. */
object TestAnt {
    /** Ant and `ant-junit` with their dependencies. */
    val classpath: List<File> = files("antBridge.test.antClasspath")

    /** The ant-contrib jar, without the Ant it depends on. */
    val contribClasspath: List<File> = files("antBridge.test.contribClasspath")

    /**
     * Makes [project]'s `antBridge` classpath resolve offline: [classpath] (already resolved by this build) as a file
     * dependency, and the plugin's default `org.apache.ant` module dependencies excluded. Tests that resolve the
     * `antBridge` configuration use it instead of a repository: a `ProjectBuilder` project and each Gradle version's
     * TestKit directory start with an empty Gradle user home, so resolving from Maven Central would download Ant on
     * every run, and Maven Central rate-limits CI runners. The default dependencies themselves are covered by
     * `AntBridgePluginSpec`.
     */
    fun useIn(project: Project) {
        project.dependencies.add("antBridge", project.files(classpath))
        project.configurations.getByName("antBridgeClasspath").exclude(mapOf("group" to "org.apache.ant"))
    }

    /** [classpath] as a comma-separated list of quoted paths, for generated build scripts. */
    val classpathLiteral: String = classpath.joinToString(", ") {
        "\"" + it.absolutePath.replace("\\", "/") + "\""
    }

    private fun files(property: String): List<File> =
        System.getProperty(property).split(File.pathSeparator).filter { it.isNotEmpty() }.map(::File)
}

