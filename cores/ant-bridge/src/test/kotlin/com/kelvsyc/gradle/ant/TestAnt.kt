package com.kelvsyc.gradle.ant

import java.io.File

/** Ant classpaths for tests, resolved by the build and passed as system properties. */
object TestAnt {
    /** Ant and `ant-junit` with their dependencies. */
    val classpath: List<File> = files("antBridge.test.antClasspath")

    /** The ant-contrib jar, without the Ant it depends on. */
    val contribClasspath: List<File> = files("antBridge.test.contribClasspath")

    private fun files(property: String): List<File> =
        System.getProperty(property).split(File.pathSeparator).filter { it.isNotEmpty() }.map(::File)
}

