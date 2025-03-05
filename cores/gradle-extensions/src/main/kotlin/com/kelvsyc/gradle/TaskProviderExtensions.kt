package com.kelvsyc.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

// Internal workaround since Gradle libraries do not treat Action<in T> as T.() -> Unit
internal fun <T : Task> TaskProvider<T>.configureKt(action: T.() -> Unit) = configure(action)

/**
 * Configures the task with an additional action.
 *
 * Syntactic shorthand for [configure][TaskProvider.configure]` { `[doLast][Task.doLast]` { action(this) } }`
 */
fun <T : Task> TaskProvider<T>.doLast(action: T.() -> Unit) = configureKt {
    doLast {
        action(this)
    }
}

/**
 * Configures the task with an additional action.
 *
 * Syntactic shorthand for [configure][TaskProvider.configure]` { `[doFirst][Task.doFirst]` { action(this) } }`
 */
fun <T : Task> TaskProvider<T>.doFirst(action: T.() -> Unit) = configureKt {
    doFirst {
        action(this)
    }
}
