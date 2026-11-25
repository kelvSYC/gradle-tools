package com.kelvsyc.gradle.ant

import groovy.lang.Closure
import groovy.lang.GString
import groovy.lang.GroovyInterceptable
import groovy.lang.MetaClass
import groovy.lang.MissingPropertyException
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.GradleException

/**
 * Thrown when a recorded Ant block tries to read a value that only exists once Ant has run.
 *
 * It is a [MissingPropertyException] so that, in a closure with a recorder as its delegate, Groovy falls back to the
 * closure's owner for names the recorder does not have, and rethrows this exception only if the owner has none either.
 */
class AntReadBackException(
    private val readBackMessage: String,
    cause: Throwable? = null
) : MissingPropertyException(readBackMessage, null as String?, null as Class<*>?) {
    init {
        cause?.let { initCause(it) }
    }

    override val message: String get() = readBackMessage

    override fun getMessageWithoutLocationText(): String = readBackMessage
}

/**
 * Thrown when a recorded Ant block reads `properties`, `references` or `project` from the recorder. Unlike
 * [AntReadBackException] it is not a [MissingPropertyException], so a closure never falls back to an owner's property
 * of the same name, which would silently return a Gradle value instead of Ant's.
 */
class AntReservedPropertyException(message: String) : GradleException(message)

/**
 * Records legacy `ant.xxx(...)` calls into [AntElement] trees instead of running them. A method name becomes the
 * element name (`"uri:local"` is split at the last `:` into namespace and name), a map argument becomes attributes, a
 * string argument becomes the text and a trailing closure records the children, with a recorder as its delegate.
 *
 * Ant runs only after the block ends: reading `properties`, `references` or `project` throws
 * [AntReservedPropertyException]; reading or setting any other property, or using the value a recorded call returns,
 * throws [AntReadBackException]. In a closure with a recorder as its delegate, other names still resolve against the
 * closure's owner. An `ant.` prefix is accepted: [ant] is the recorder itself.
 */
class AntRecorder internal constructor(private val sink: MutableList<AntElement>) : GroovyInterceptable {
    /**
     * This recorder, so that legacy bodies written as `ant.copy(...)` record here rather than reaching Gradle's own
     * `project.ant`: in Groovy, `ant` is read from the recorder before the closure's owner; in Kotlin,
     * `record { ant.withGroovyBuilder { ... } }` would otherwise bind to `Project.ant`.
     */
    val ant: AntRecorder get() = this

    override fun invokeMethod(name: String, args: Any?): Any? {
        val arguments = argumentsOf(args)
        return when (name) {
            "toString" -> toString()
            "hashCode" -> hashCode()
            "equals" -> equals(arguments.firstOrNull())
            else -> record(name, arguments)
        }
    }

    override fun getProperty(propertyName: String): Any? {
        if (propertyName == "ant") return this
        if (propertyName in RECORDER_PROPERTIES) {
            throw AntReservedPropertyException(readMessage(propertyName))
        }
        throw AntReadBackException(readMessage(propertyName))
    }

    override fun setProperty(propertyName: String, newValue: Any?) {
        throw AntReadBackException("Cannot set '$propertyName' on a recorded Ant block: Ant runs after the block ends.")
    }

    override fun getMetaClass(): MetaClass = InvokerHelper.getMetaClass(javaClass)

    override fun setMetaClass(metaClass: MetaClass?) {
        throw UnsupportedOperationException("The meta class of an Ant recorder cannot be replaced")
    }

    private fun record(qualifiedName: String, arguments: List<Any?>): RecordedCall {
        val separator = qualifiedName.lastIndexOf(':')
        val element = if (separator < 0) {
            AntElement(qualifiedName)
        } else {
            AntElement(qualifiedName.substring(separator + 1), qualifiedName.substring(0, separator))
        }
        sink += element
        arguments.forEachIndexed { index, argument ->
            when {
                argument is Map<*, *> ->
                    argument.forEach { (key, value) ->
                        val attribute = key.toString()
                        element.attributes[attribute] = checked(value, "Attribute '$attribute'", qualifiedName)
                    }
                argument is CharSequence -> element.text = checked(argument, "The text", qualifiedName).toString()
                argument is Closure<*> && index == arguments.lastIndex -> recordChildren(element, argument)
                else -> throw IllegalArgumentException(
                    "Unsupported argument $argument for recorded Ant element '$qualifiedName': " +
                        "use a map of attributes, a text string and a trailing closure"
                )
            }
        }
        return RecordedCall()
    }

    private fun recordChildren(element: AntElement, closure: Closure<*>) {
        val child = AntRecorder(element.children)
        val body = closure.rehydrate(child, closure.owner, closure.thisObject)
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
    }

    /** Entry point for recording a block. */
    internal companion object {
        private val RECORDER_PROPERTIES = setOf("properties", "references", "project")

        /**
         * Runs [body] with a fresh recorder and returns the top-level elements it recorded.
         */
        fun record(body: (AntRecorder) -> Unit): List<AntElement> {
            val sink = ArrayList<AntElement>()
            body(AntRecorder(sink))
            return sink
        }

        private fun argumentsOf(args: Any?): List<Any?> = when (args) {
            null -> emptyList()
            is Array<*> -> args.asList()
            else -> listOf(args)
        }

        private fun checked(value: Any?, what: String, elementName: String): Any? {
            val recorded = value is RecordedCall || value is GString && value.values.any { it is RecordedCall }
            if (recorded) {
                throw AntReadBackException(
                    "$what of recorded Ant element '$elementName' uses the value of a recorded Ant call, which is " +
                        "not available: Ant runs after the block ends. Inside a recorded block an unqualified method " +
                        "call such as file(...) is recorded as an Ant element; compute the value outside the block."
                )
            }
            return if (value is GString) value.toString() else value
        }

        private fun readMessage(name: String) =
            "Cannot read '$name' from a recorded Ant block: Ant runs after the block ends. " +
                "Load properties with antBridge.properties { } (a Provider) or read a task output instead."
    }
}

/**
 * The value returned by a recorded Ant call. Ant runs only after the block ends, so any use of it throws
 * [AntReadBackException].
 */
class RecordedCall internal constructor() : GroovyInterceptable {
    override fun invokeMethod(name: String, args: Any?): Any? = throw notAvailable()

    override fun getProperty(propertyName: String): Any? = throw notAvailable()

    override fun setProperty(propertyName: String, newValue: Any?) = throw notAvailable()

    override fun getMetaClass(): MetaClass = InvokerHelper.getMetaClass(javaClass)

    override fun setMetaClass(metaClass: MetaClass?) {
        throw UnsupportedOperationException("The meta class of a recorded Ant call cannot be replaced")
    }

    override fun toString(): String = "<recorded Ant call; no value>"

    private fun notAvailable() =
        AntReadBackException("The value returned by a recorded Ant call is not available: Ant runs after the block ends.")
}
