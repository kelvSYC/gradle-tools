package com.kelvsyc.gradle.ant.internal

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Properties

/**
 * Reads and writes `.properties` files deterministically, in the format Ant's `property file=` reads.
 */
internal object PropertiesFiles {
    /**
     * Writes [values] to [file], creating parent directories: keys sorted, no comments or timestamp, and
     * [Properties] escaping (ISO 8859-1 with `\uXXXX`).
     */
    fun write(file: File, values: Map<String, String>) {
        val lines = values.toSortedMap().map { (key, value) ->
            val out = ByteArrayOutputStream()
            Properties().apply { setProperty(key, value) }.store(out, null)
            out.toString(Charsets.ISO_8859_1).lines().filter { it.isNotEmpty() && !it.startsWith("#") }
        }.flatten()
        file.absoluteFile.parentFile?.mkdirs()
        file.writeText(lines.joinToString("") { "$it\n" }, Charsets.ISO_8859_1)
    }

    /**
     * Reads [file], returning its entries in sorted key order.
     */
    fun read(file: File): Map<String, String> {
        val properties = Properties()
        file.inputStream().use { properties.load(it) }
        val result = LinkedHashMap<String, String>()
        properties.stringPropertyNames().sorted().forEach { result[it] = properties.getProperty(it) }
        return result
    }
}
