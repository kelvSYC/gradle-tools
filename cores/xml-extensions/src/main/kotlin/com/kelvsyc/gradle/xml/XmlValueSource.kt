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
