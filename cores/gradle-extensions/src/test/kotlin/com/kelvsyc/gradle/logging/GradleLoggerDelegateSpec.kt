package com.kelvsyc.gradle.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import kotlin.reflect.KProperty

class GradleLoggerDelegateSpec : FunSpec() {
    @Suppress("detekt:UtilityClassWithPublicConstructor")
    private class WithCompanion {
        companion object {
            val logger by GradleLoggerDelegate
        }
    }

    private class PlainClass

    init {
        val property = mockk<KProperty<*>>()

        test("returns task logger for Task thisRef") {
            val task = mockk<Task>()
            val taskLogger = mockk<Logger>()
            every { task.logger } returns taskLogger

            val result = GradleLoggerDelegate.getValue(task, property)

            result shouldBeSameInstanceAs taskLogger
        }

        test("returns project logger for Project thisRef") {
            val project = mockk<Project>()
            val projectLogger = mockk<Logger>()
            every { project.logger } returns projectLogger

            val result = GradleLoggerDelegate.getValue(project, property)

            result shouldBeSameInstanceAs projectLogger
        }

        test("returns logger for enclosing class when used in companion object") {
            val result = WithCompanion.logger

            result.name shouldBeEqual WithCompanion::class.java.name
        }

        test("returns logger for the class itself when not a companion object") {
            val instance = PlainClass()
            val expected = Logging.getLogger(PlainClass::class.java)

            val result = GradleLoggerDelegate.getValue(instance, property)

            result.name shouldBeEqual expected.name
        }
    }
}
