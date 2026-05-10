package com.kelvsyc.gradle.xml

import com.kelvsyc.kotlin.xml.XmlElement
import com.kelvsyc.kotlin.xml.parseXml
import org.gradle.api.provider.Provider

/**
 * Returns a [Provider] that lazily parses this string provider's value as an [XmlElement] tree.
 */
fun Provider<String>.parseXml(): Provider<XmlElement> = map { it.parseXml() }
