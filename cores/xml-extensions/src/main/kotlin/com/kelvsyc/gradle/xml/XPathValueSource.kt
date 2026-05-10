package com.kelvsyc.gradle.xml

import com.kelvsyc.kotlin.xml.XPath
import com.kelvsyc.kotlin.xml.parseXml
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException
import javax.xml.stream.XMLStreamException

/**
 * Gradle [ValueSource] that extracts a single string value from an XML file using an XPath
 * expression.
 *
 * The matched node's string-value is returned: for elements this is the concatenation of all
 * descendant text, for attributes this is the attribute value, and for text nodes this is the
 * text content.
 *
 * If the input file is not found, cannot be parsed, or the path matches zero or multiple nodes,
 * no value will be provided.
 */
abstract class XPathValueSource : ValueSource<String, XPathValueSource.Parameters> {
    /**
     * Parameters for [XPathValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The XML input file.
         */
        val inputFile: RegularFileProperty

        /**
         * The XPath expression to evaluate.
         */
        val xpath: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val root = parameters.inputFile.get().asFile.inputStream().use { it.parseXml() }
            val path = XPath.parse(parameters.xpath.get())
            path.queryOne(root)?.stringValue
        } catch (_: IOException) {
            null
        } catch (_: XMLStreamException) {
            null
        }
    }
}
