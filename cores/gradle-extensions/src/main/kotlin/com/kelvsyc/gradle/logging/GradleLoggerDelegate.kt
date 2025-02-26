package com.kelvsyc.gradle.logging

import org.gradle.api.Project
import org.gradle.api.Script
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

/**
 * Kotlin delegate that can be used to supply a Gradle logger. This logger should be used within a companion object,
 * like so:
 *
 * ```kotlin
 * companion object {
 *     val logger by GradleLoggerDelegate
 * }
 * ```
 *
 * Note that this delegate can also be used outside of a companion object as well. For some classes such as [Task] or
 * [Project], this delegate will instead delegate to their respective `getLogger()` methods.
 */
object GradleLoggerDelegate : ReadOnlyProperty<Any, Logger> {
    /**
     * Returns the Java [Class] object representing the base class, given the Java [Class] object representing its
     * companion object.
     *
     * @return `null` if the class is not a companion object class, or the base class if it is.
     */
    private fun <T : Any> getBaseClassFromCompanion(javaClass: Class<T>): Class<*>? {
        return javaClass.enclosingClass?.takeIf { javaClass.enclosingClass.kotlin.companionObject?.java == javaClass }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        // Some Gradle methods have inherent logger methods which we should delegate to
        return when (thisRef) {
            is Task -> thisRef.logger
            is Project -> thisRef.logger
            is Script -> thisRef.logger
            else -> {
                // In all other cases, we look up the appropriate logger class
                val loggerClass = getBaseClassFromCompanion(thisRef.javaClass) ?: thisRef.javaClass
                Logging.getLogger(loggerClass)
            }
        }
    }
}
