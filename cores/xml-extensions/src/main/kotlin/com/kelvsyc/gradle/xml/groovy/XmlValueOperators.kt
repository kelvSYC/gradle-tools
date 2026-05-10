package com.kelvsyc.gradle.xml.groovy

import com.kelvsyc.kotlin.xml.XmlElement

/**
 * Navigates into this [XmlElement] by child element name, returning the first matching child
 * or `null` if no child with that name exists.
 *
 * This extension is a **migration aid** for codebases transitioning from Groovy's `XmlSlurper`,
 * where dynamic property access like `root.dependencies.dependency` is idiomatic. In Kotlin,
 * prefer [XmlElement.element] or XPath queries via [com.kelvsyc.kotlin.xml.XmlElement.query]
 * once the migration is complete.
 *
 * Remove the import of this package when the migration is finished.
 */
operator fun XmlElement.get(name: String): XmlElement? = element(name)

/**
 * Navigates into the child elements of this [XmlElement] by positional index, returning the
 * child element at that position or `null` if the index is out of bounds.
 *
 * This extension is a **migration aid** for codebases transitioning from Groovy's `XmlSlurper`,
 * where indexed access like `root.dependency[0]` works on GPathResult collections. In Kotlin,
 * prefer [XmlElement.elements] with standard list indexing or XPath positional predicates
 * once the migration is complete.
 *
 * Remove the import of this package when the migration is finished.
 */
operator fun XmlElement.get(index: Int): XmlElement? = elements().getOrNull(index)
