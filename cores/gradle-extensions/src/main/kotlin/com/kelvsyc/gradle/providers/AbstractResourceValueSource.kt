package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.InputStream

/**
 * Base class for [ValueSource] implementations that provide a value from reading a resource bundled in the plugin JAR.
 *
 * Subclasses should implement the [doObtain] function, transforming an [InputStream] object to an object of the
 * desired type.
 */
abstract class AbstractResourceValueSource<T : Any, P : AbstractResourceValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractResourceValueSource]. This contains the data needed to locate a resource
     * on the classpath.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractResourceValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The classpath resource path to read.
         */
        val resourcePath: Property<String>
    }

    /**
     * Transforms the data retrieved from the classpath resource.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The transformed data, or `null` if the data cannot be transformed into the needed type.
     */
    abstract fun doObtain(input: InputStream): T?

    override fun obtain(): T? {
        val stream = this.javaClass.classLoader.getResourceAsStream(parameters.resourcePath.get()) ?: return null
        return stream.use(::doObtain)
    }
}
