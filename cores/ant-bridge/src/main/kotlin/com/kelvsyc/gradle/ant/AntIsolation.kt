package com.kelvsyc.gradle.ant

/**
 * How Ant work is isolated from the Gradle build.
 */
enum class AntIsolation {
    /** Runs Ant in the Gradle daemon, in an isolated class loader. The default. */
    CLASSLOADER,

    /** Runs Ant in a separate worker process, for Ant tasks that leak static state or call `System.exit`. */
    PROCESS
}
