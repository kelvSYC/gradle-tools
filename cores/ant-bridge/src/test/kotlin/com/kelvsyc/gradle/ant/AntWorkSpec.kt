package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.plugins.AntBridgePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.TaskOutputsInternal
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

    test("the tree fingerprint is a hash that follows the resolved tree") {
        val project = project()
        var count = 0
        fun fingerprint(message: String): String {
            val task = project.tasks.register("antWork${count++}", AntWork::class.java).get()
            task.ant { task("echo", "message" to project.provider { message }) }
            return task.treeFingerprint.get()
        }
        fingerprint("secret-one") shouldBe fingerprint("secret-one")
        fingerprint("secret-one") shouldNotBe fingerprint("secret-two")
        fingerprint("secret-one") shouldMatch Regex("[0-9a-f]{64}")
    }

    test("the tree fingerprint is a task input") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.inputs.properties.keys shouldContain "treeFingerprint"
    }

    test("the extension exposes one AntOperations") {
        val extension = project().extensions.getByType(AntBridgeExtension::class.java)
        extension.operations shouldBeSameInstanceAs extension.operations
    }

    test("no exports means no export output") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.exportedPropertiesFile.isPresent shouldBe false
        task.outputs.hasOutput shouldBe false
    }

    test("exportProperties declares the file and the names as input and output") {
        val project = project()
        val task = project.tasks.register("antWork", AntWork::class.java).get()
        task.exportProperties("rev")
        task.exportedPropertiesFile.get().asFile.parentFile shouldBe
            project.layout.buildDirectory.dir("ant-bridge/antWork").get().asFile
        task.outputs.files.files shouldContain task.exportedPropertiesFile.get().asFile
        @Suppress("UNCHECKED_CAST")
        (task.inputs.properties["exportedPropertyNames"] as Set<String>) shouldContain "rev"
    }

    test("exportedProperties carries the task dependency") {
        val project = project()
        val work = project.tasks.register("antWork", AntWork::class.java) { exportProperties("rev") }
        val consumer = project.tasks.register("consumer") {
            inputs.property("v", work.flatMap { it.exportedProperties })
        }.get()
        consumer.taskDependencies.getDependencies(consumer) shouldContain work.get()
    }

    test("exportProperties without names registers nothing") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.exportProperties()
        task.exportedPropertiesFile.isPresent shouldBe false
        task.outputs.hasOutput shouldBe false
    }

    test("an export alone never satisfies the up-to-date spec") {
        val task = project().tasks.register("antWork", AntWork::class.java).get()
        task.exportProperties("rev")
        (task.outputs as TaskOutputsInternal).upToDateSpec.isSatisfiedBy(task as TaskInternal) shouldBe false
    }

    test("an export with another declared output satisfies the up-to-date spec") {
        val project = project()
        val task = project.tasks.register("antWork", AntWork::class.java).get()
        task.exportProperties("rev")
        val other = project.file("out.txt").apply { writeText("x") }
        task.outputs.file(other)
        (task.outputs as TaskOutputsInternal).upToDateSpec.isSatisfiedBy(task as TaskInternal) shouldBe true
    }
})
