package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.plugins.AntBridgePlugin
import java.io.File
import java.net.URLClassLoader

/**
 * An isolated class loader for Ant and its antlibs, whose parent is the platform class loader, so that neither
 * Gradle's own Ant nor the plugin's classes are visible to Ant. The shim classes (`com.kelvsyc.gradle.ant.shim`)
 * are defined in it from the plugin's `ant-bridge-shim/<path>.class.bin` resources.
 *
 * See the core's `INTERNALS.md` for why Ant is isolated this way.
 */
class AntRuntimeClassLoader(antClasspath: List<File>) :
    URLClassLoader(antClasspath.map { it.toURI().toURL() }.toTypedArray(), getPlatformClassLoader()) {

    override fun findClass(name: String): Class<*> {
        if (name.startsWith(SHIM_PACKAGE)) {
            val resource = "$SHIM_RESOURCE_ROOT/${name.replace('.', '/')}.class.bin"
            val bytes = AntBridgePlugin::class.java.classLoader.getResourceAsStream(resource)?.use { it.readBytes() }
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.size)
            }
        }
        return super.findClass(name)
    }

    private companion object {
        const val SHIM_PACKAGE = "com.kelvsyc.gradle.ant.shim."
        const val SHIM_RESOURCE_ROOT = "ant-bridge-shim"
    }
}
