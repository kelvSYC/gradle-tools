package com.kelvsyc.gradle.xml

import com.kelvsyc.kotlin.xml.XmlElement
import com.kelvsyc.kotlin.xml.parseXml
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException
import javax.xml.stream.XMLStreamException

/**
 * Gradle [ValueSource] that provides an [XmlElement] tree parsed from an XML file.
 *
 * The parser is non-validating, does not process DTDs, and disables external entity resolution.
 *
 * If the input file is not found or cannot be parsed, no value will be provided.
 *
 * **Configuration cache and sensitive files:** Gradle serializes the entire [XmlElement] tree to the configuration
 * cache in plaintext when the cache is written. If the XML file contains sensitive values — passwords, tokens, API
 * keys — those values will be stored in `.gradle/configuration-cache/`. This applies regardless of how the
 * resulting [org.gradle.api.provider.Provider] is stored: a task `@Input`, `@get:Internal`, or private `val` all
 * cause `obtain()` to run at configuration time and the parsed tree to be cached. For files containing sensitive
 * data, read and use the file entirely within a `@TaskAction` or [org.gradle.workers.WorkAction.execute] body
 * instead.
 */
abstract class XmlValueSource : ValueSource<XmlElement, XmlValueSource.Parameters> {
    /**
     * Parameters for [XmlValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The XML input file.
         */
        val inputFile: RegularFileProperty
    }

    override fun obtain(): XmlElement? {
        return try {
            parameters.inputFile.get().asFile.inputStream().use { it.parseXml() }
        } catch (_: IOException) {
            null
        } catch (_: XMLStreamException) {
            null
        }
    }
}
