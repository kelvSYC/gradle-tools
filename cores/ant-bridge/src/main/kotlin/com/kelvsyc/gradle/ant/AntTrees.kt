package com.kelvsyc.gradle.ant

import groovy.lang.Closure
import org.gradle.api.Action

/**
 * Builds Ant trees with either front end, for submitting [com.kelvsyc.gradle.ant.actions.AntWorkAction] yourself.
 * [AntOperations] and [AntWork] use the same builders.
 *
 * The returned elements keep providers and files lazy; call [AntElement.resolve] on each at execution time to get the
 * action's `nodes`.
 */
object AntTrees {
    /**
     * Returns the Ant tasks of [body], written in the Kotlin DSL.
     */
    @JvmStatic
    fun build(body: Action<in AntScope>): List<AntElement> {
        val sink = ArrayList<AntElement>()
        body.execute(AntScope(sink))
        return sink
    }

    /**
     * Returns the legacy `ant.xxx(...)` calls recorded by [body], such as a Kotlin `withGroovyBuilder { }` block.
     */
    @JvmStatic
    fun record(body: Action<in AntRecorder>): List<AntElement> = AntRecorder.record { body.execute(it) }

    /**
     * Returns the legacy `ant.xxx(...)` calls recorded by the Groovy closure [body], which runs with the recorder as
     * its delegate, so its body is unchanged from a legacy `ant { }` block.
     */
    @JvmStatic
    fun record(body: Closure<*>): List<AntElement> = AntRecorder.record { recorder ->
        val delegated = body.rehydrate(recorder, body.owner, body.thisObject)
        delegated.resolveStrategy = Closure.DELEGATE_FIRST
        delegated.call(recorder)
    }
}
