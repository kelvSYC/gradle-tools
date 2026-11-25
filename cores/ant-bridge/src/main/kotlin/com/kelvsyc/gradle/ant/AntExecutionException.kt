package com.kelvsyc.gradle.ant

import org.gradle.api.GradleException

/**
 * Thrown when Ant reports a build failure; the message is Ant's, prefixed with its location when Ant knows one.
 */
class AntExecutionException(message: String) : GradleException(message)
