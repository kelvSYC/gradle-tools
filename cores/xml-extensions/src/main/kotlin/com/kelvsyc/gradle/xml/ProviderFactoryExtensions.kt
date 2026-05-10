package com.kelvsyc.gradle.xml

import com.kelvsyc.kotlin.xml.XmlElement
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of
import kotlin.reflect.KClass

private fun <T : Any, P : ValueSourceParameters> ProviderFactory.ofKt(
    valueSourceType: KClass<out ValueSource<T, P>>,
    configuration: ValueSourceSpec<P>.() -> Unit,
) = of(valueSourceType, configuration)

/**
 * Returns a [Provider] providing an [XmlElement] tree parsed from an XML file.
 *
 * @param file the XML file to parse
 * @see XmlValueSource
 */
fun ProviderFactory.xmlFile(file: RegularFile): Provider<XmlElement> = ofKt(XmlValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing an [XmlElement] tree parsed from an XML file.
 *
 * @param file a provider for the XML file to parse
 * @see XmlValueSource
 */
fun ProviderFactory.xmlFile(file: Provider<RegularFile>): Provider<XmlElement> = ofKt(XmlValueSource::class) {
    parameters.inputFile.set(file)
}

/**
 * Returns a [Provider] providing a string value extracted from an XML file using an XPath
 * expression.
 *
 * @param file the XML file to parse
 * @param xpath the XPath expression to evaluate
 * @see XPathValueSource
 */
fun ProviderFactory.xpath(file: RegularFile, xpath: String): Provider<String> =
    ofKt(XPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.xpath.set(xpath)
    }

/**
 * Returns a [Provider] providing a string value extracted from an XML file using an XPath
 * expression.
 *
 * @param file a provider for the XML file to parse
 * @param xpath the XPath expression to evaluate
 * @see XPathValueSource
 */
fun ProviderFactory.xpath(file: Provider<RegularFile>, xpath: String): Provider<String> =
    ofKt(XPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.xpath.set(xpath)
    }

/**
 * Returns a [Provider] providing a string value extracted from an XML file using an XPath
 * expression.
 *
 * @param file the XML file to parse
 * @param xpath a provider for the XPath expression to evaluate
 * @see XPathValueSource
 */
fun ProviderFactory.xpath(file: RegularFile, xpath: Provider<String>): Provider<String> =
    ofKt(XPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.xpath.set(xpath)
    }

/**
 * Returns a [Provider] providing a string value extracted from an XML file using an XPath
 * expression.
 *
 * @param file a provider for the XML file to parse
 * @param xpath a provider for the XPath expression to evaluate
 * @see XPathValueSource
 */
fun ProviderFactory.xpath(file: Provider<RegularFile>, xpath: Provider<String>): Provider<String> =
    ofKt(XPathValueSource::class) {
        parameters.inputFile.set(file)
        parameters.xpath.set(xpath)
    }
