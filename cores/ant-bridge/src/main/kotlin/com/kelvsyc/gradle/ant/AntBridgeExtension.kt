package com.kelvsyc.gradle.ant

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * The `antBridge` extension. Not designed to be extended.
 */
abstract class AntBridgeExtension @Inject constructor(
    /** The resolved Ant classpath: Ant, its antlibs and anything else declared on `antBridge`. */
    val classpath: FileCollection,
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
) {
    /** Runs Ant from task actions and `doLast` blocks at execution time. */
    val operations: AntOperations = objects.newInstance(AntOperations::class.java)

    /**
     * Returns the properties declared by [configure], loaded by [AntProperties] with [classpath]. Each obtain opens
     * and closes its own class loader.
     */
    fun properties(configure: Action<in AntPropertiesSpec>): Provider<Map<String, String>> {
        val spec = objects.newInstance(AntPropertiesSpec::class.java)
        configure.execute(spec)
        return providers.of(AntProperties::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.from(classpath)
            }
        }
    }
}
