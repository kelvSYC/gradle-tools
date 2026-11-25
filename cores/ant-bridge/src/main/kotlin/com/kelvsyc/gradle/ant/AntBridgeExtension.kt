package com.kelvsyc.gradle.ant

import org.gradle.api.file.FileCollection
import javax.inject.Inject

/**
 * The `antBridge` extension.
 */
abstract class AntBridgeExtension @Inject constructor(
    /** The resolved Ant classpath: Ant, its antlibs and anything else declared on `antBridge`. */
    val classpath: FileCollection
)

