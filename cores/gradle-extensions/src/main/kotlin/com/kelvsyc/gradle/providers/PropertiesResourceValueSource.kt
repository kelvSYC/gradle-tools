package com.kelvsyc.gradle.providers

import org.gradle.api.provider.ValueSource
import java.io.InputStream
import java.util.Properties

/**
 * A [ValueSource] that reads a classpath resource as a [Properties] object.
 */
abstract class PropertiesResourceValueSource :
    AbstractResourceValueSource<Properties, AbstractResourceValueSource.Parameters>() {
    override fun doObtain(input: InputStream): Properties = Properties().apply { load(input) }
}
