package com.kelvsyc.gradle.providers

import org.gradle.api.provider.ValueSource
import java.io.InputStream

/**
 * A [ValueSource] that reads a classpath resource as a [String].
 */
abstract class StringResourceValueSource :
    AbstractResourceValueSource<String, AbstractResourceValueSource.Parameters>() {
    override fun doObtain(input: InputStream): String = input.bufferedReader().readText()
}
