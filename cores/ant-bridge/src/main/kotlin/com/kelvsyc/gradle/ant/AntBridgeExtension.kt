package com.kelvsyc.gradle.ant

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * The `antBridge` extension.
 */
abstract class AntBridgeExtension @Inject constructor(
    /** The resolved Ant classpath: Ant, its antlibs and anything else declared on `antBridge`. */
    val classpath: FileCollection
) {
    /** Creates the property specs. */
    @get:Inject
    protected abstract val objects: ObjectFactory

    /** Creates the property value sources. */
    @get:Inject
    protected abstract val providers: ProviderFactory

    /** Runs Ant from task actions and `doLast` blocks at execution time. */
    val operations: AntOperations = objects.newInstance(AntOperations::class.java)

    /**
     * Returns the properties declared by [configure], loaded by [AntProperties] with [classpath]. Each obtain uses
     * its own class loader: the value is typically obtained at configuration time, where the configuration cache does
     * not support build services.
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
