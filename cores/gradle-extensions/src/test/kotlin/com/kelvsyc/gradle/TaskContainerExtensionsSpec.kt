package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import kotlin.jvm.java

class TaskContainerExtensionsSpec : FunSpec() {
    init {
        test("classes") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.classesTaskName } returns name

            container.classes(sourceSet)

            verify {
                container.named(name)
            }
        }

        test("compile") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.getCompileTaskName(any()) } returns name

            container.compile<Task>(sourceSet, "baz")

            verifySequence {
                sourceSet.getCompileTaskName("baz")
                container.named(name, Task::class.java)
            }
        }

        test("compileJava") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.compileJavaTaskName } returns name

            container.compileJava(sourceSet)

            verify {
                container.named(name, JavaCompile::class.java)
            }
        }

        test("jar") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.jarTaskName } returns name

            container.jar(sourceSet)

            verify {
                container.named(name, Jar::class.java)
            }
        }

        test("javadocJar") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.javadocJarTaskName } returns name

            container.javadocJar(sourceSet)

            verify {
                container.named(name, Jar::class.java)
            }
        }

        test("processResources") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.processResourcesTaskName } returns name

            container.processResources<Copy>(sourceSet)

            verify {
                container.named(name, Copy::class.java)
            }
        }

        test("sourcesJar") {
            val container = mockk<TaskContainer>(relaxed = true)
            val sourceSet = mockk<SourceSet>(relaxed = true)
            val name = "foobar"
            every { sourceSet.sourcesJarTaskName } returns name

            container.sourcesJar(sourceSet)

            verify {
                container.named(name, Jar::class.java)
            }
        }
    }
}
