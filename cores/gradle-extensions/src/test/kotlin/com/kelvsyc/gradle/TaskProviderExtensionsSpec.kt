package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

class TaskProviderExtensionsSpec : FunSpec() {
    init {
        test("doFirst") {
            val project = ProjectBuilder.builder().build()
            val provider = project.tasks.register("task")
            val spy = spyk(provider)
            val slot = slot<Action<Task>>()

            val action = mockk<Task.() -> Unit>(relaxed = true)

            spy.doFirst(action)

            verify {
                // Verifies that provider's configure() method is called
                spy.configure(capture(slot))
            }

            // Verify that the task's doFirst is called when we run the configure action
            val task = spyk(provider.get())
            val slot2 = slot<Action<Task>>()
            slot.captured.execute(task)
            verify {
                task.doFirst(capture(slot2))
            }

            // Verify that the action itself is called when the doFirst block is run
            slot2.captured.execute(task)
            verify {
                action.invoke(task)
            }
        }

        test("doLast") {
            val project = ProjectBuilder.builder().build()
            val provider = project.tasks.register("task")
            val spy = spyk(provider)
            val slot = slot<Action<Task>>()

            val action = mockk<Task.() -> Unit>(relaxed = true)

            spy.doLast(action)

            verify {
                // Verifies that provider's configure() method is called
                spy.configure(capture(slot))
            }

            // Verify that the task's doLast is called when we run the configure action
            val task = spyk(provider.get())
            val slot2 = slot<Action<Task>>()
            slot.captured.execute(task)
            verify {
                task.doLast(capture(slot2))
            }

            // Verify that the action itself is called when the doLast block is run
            slot2.captured.execute(task)
            verify {
                action.invoke(task)
            }
        }
    }
}
