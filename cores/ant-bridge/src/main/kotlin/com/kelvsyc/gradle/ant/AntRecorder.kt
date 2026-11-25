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
 */
class AntReadBackException(message: String, cause: Throwable? = null) : GradleException(message, cause)

/**
 * Records legacy `ant.xxx(...)` calls into [AntElement] trees instead of running them. A method name becomes the
 * element name (`"uri:local"` is split at the last `:` into namespace and name), a map argument becomes attributes, a
 * string argument becomes the text and a trailing closure records the children, with a recorder as its delegate.
 *
 * Ant runs only after the block ends, so reading a property from the recorder, or using the value a recorded call
 * returns, throws [AntReadBackException]. Inside a nested closure, names the recorder does not own resolve against
 * the closure's owner as usual.
 */
class AntRecorder internal constructor(
    private val sink: MutableList<AntElement>,
    private val owner: Any? = null
) : GroovyInterceptable {
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
        val scope = owner
        if (scope == null || propertyName in RECORDER_PROPERTIES) {
            throw AntReadBackException(readMessage(propertyName))
        }
        return try {
            InvokerHelper.getProperty(scope, propertyName)
        } catch (e: MissingPropertyException) {
            throw AntReadBackException(readMessage(propertyName), e)
        }
    }

    override fun setProperty(propertyName: String, newValue: Any?) {
        val scope = owner ?: throw AntReadBackException(writeMessage(propertyName))
        try {
            InvokerHelper.setProperty(scope, propertyName, newValue)
        } catch (e: MissingPropertyException) {
            throw AntReadBackException(writeMessage(propertyName), e)
        }
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
                    argument.forEach { (key, value) -> element.attributes[key.toString()] = normalize(value) }
                argument is CharSequence -> element.text = argument.toString()
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
        val child = AntRecorder(element.children, closure.owner)
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

        private fun normalize(value: Any?): Any? = if (value is GString) value.toString() else value

        private fun readMessage(name: String) =
            "Cannot read '$name' from a recorded Ant block: Ant runs after the block ends. " +
                "Load properties with antBridge.properties { } (a Provider) or read a task output instead."

        private fun writeMessage(name: String) =
            "Cannot set '$name' on a recorded Ant block: Ant runs after the block ends."
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
