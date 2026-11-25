package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.plugins.AntBridgePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class AntWorkSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().build().also { it.plugins.apply(AntBridgePlugin::class.java) }

    test("plugin wires the classpath and default isolation") {
        val project = project()
        val task = project.tasks.register("antWork", AntWork::class.java).get()
        val extension = project.extensions.getByType(AntBridgeExtension::class.java)
        task.antClasspath.from shouldContain extension.classpath
        task.isolation.get() shouldBe AntIsolation.CLASSLOADER
    }

    test("ant and record append in call order") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.ant { task("mkdir", "dir" to "a") }
        task.record { invokeMethod("echo", arrayOf(mapOf("message" to "hi"))) }
        task.ant { task("delete", "dir" to "a") }
        task.elements.get().map { it.name } shouldContainExactly listOf("mkdir", "echo", "delete")
    }

    test("has no outputs, so it is never up to date") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.outputs.hasOutput shouldBe false
    }

    test("the extension exposes one AntOperations") {
        val extension = project().extensions.getByType(AntBridgeExtension::class.java)
        extension.operations shouldBeSameInstanceAs extension.operations
    }
})
