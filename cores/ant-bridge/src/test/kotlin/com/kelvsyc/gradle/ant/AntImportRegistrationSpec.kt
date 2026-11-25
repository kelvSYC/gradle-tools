package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.plugins.AntBridgePlugin
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

private const val BUILD = """
<project name="p">
    <target name="init"/>
    <target name="compile" depends="init" description="Compiles"/>
    <target name="jar" depends="compile,init" description="Builds the jar"/>
</project>
"""

class AntImportRegistrationSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().withProjectDir(tempdir()).build()
        .also { it.plugins.apply(AntBridgePlugin::class.java) }

    fun Project.write(name: String, body: String): File =
        File(projectDir, name).apply { writeText("<?xml version=\"1.0\"?>\n${body.trimIndent()}\n") }

    fun Project.import(name: String, configure: AntImportSpec.() -> Unit = {}): AntImport =
        extensions.getByType<AntBridgeExtension>().importBuild(
            file(name),
            Action {
                antClasspath(files(TestAnt.classpath))
                configure()
            }
        )

    fun Task.dependencyNames(): Set<String> = taskDependencies.getDependencies(this).map { it.name }.toSet()

    fun Project.exportOf(task: String): File =
        (tasks.getByName(task) as AntImportedTarget).exportFile.get().asFile

    test("importBuild registers one task per target with description, group and depends") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml")
        val jar = project.tasks.getByName("jar")
        jar.shouldBeInstanceOf<AntImportedTarget>()
        jar.dependencyNames() shouldContainAll listOf("compile", "init")
        jar.description shouldBe "Builds the jar"
        jar.group shouldBe "ant"
        (jar as AntImportedTarget).target.get() shouldBe "jar"
    }

    test("tasks are available right after the call") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml")
        project.tasks.named("compile").get().description shouldBe "Compiles"
    }

    test("taskName renames tasks and their dependencies") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml") { taskName { "ant-$it" } }
        project.tasks.getByName("ant-jar").dependencyNames() shouldContain "ant-compile"
        project.tasks.findByName("jar") shouldBe null
    }

    test("a clash with an existing Gradle task fails and taskName resolves it") {
        val project = project()
        project.write("build.xml", BUILD)
        project.tasks.register("compile")
        val failure = shouldThrow<InvalidUserDataException> { project.import("build.xml") }
        failure.message shouldContain "compile"
        failure.message shouldContain "taskName"
        project.tasks.findByName("init") shouldBe null
        project.import("build.xml") { taskName { "ant-$it" } }
        project.tasks.getByName("ant-jar").dependencyNames() shouldContain "ant-compile"
    }

    test("a depends entry that is not a target resolves to a Gradle task") {
        val project = project()
        project.write("build.xml", """<project name="p"><target name="a" depends="gen"/></project>""")
        project.import("build.xml")
        val a = project.tasks.getByName("a")
        // Gradle's own unknown-task failure, as for any dependsOn name.
        val failure = shouldThrowAny { a.dependencyNames() }
        generateSequence(failure as Throwable?) { it.cause }.filterIsInstance<UnknownTaskException>().first()
            .message shouldContain "gen"
        project.tasks.register("gen")
        a.dependencyNames() shouldContain "gen"
    }

    test("two imports form one build: cross-file depends and a duplicate name fails") {
        val project = project()
        project.write("one.xml", """<project name="one"><target name="a" depends="b"/></project>""")
        project.write("two.xml", """<project name="two"><target name="b"/></project>""")
        project.write("three.xml", """<project name="three"><target name="b"/></project>""")
        project.import("one.xml")
        project.import("two.xml")
        project.tasks.getByName("a").dependencyNames() shouldContain "b"
        project.exportOf("a")
        (project.tasks.getByName("a") as AntImportedTarget).upstreamExports.get().map { it.asFile } shouldBe
            listOf(project.exportOf("b"))
        val failure = shouldThrow<AntExecutionException> { project.import("three.xml") }
        failure.message shouldContain "two.xml"
        failure.message shouldContain "three.xml"
    }

    test("exclude removes the task; downstream still depends on the replacement by name") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml") { exclude("compile") }
        project.tasks.findByName("compile") shouldBe null
        project.tasks.register("compile")
        project.tasks.getByName("jar").dependencyNames() shouldContain "compile"
    }

    test("upstream exports are declared inputs in Ant order") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml")
        val jar = project.tasks.getByName("jar") as AntImportedTarget
        jar.upstreamExports.get().map { it.asFile } shouldContainExactly
            listOf(project.exportOf("init"), project.exportOf("compile"))
        jar.inputs.files.files shouldContainAll listOf(project.exportOf("init"), project.exportOf("compile"))
        project.exportOf("jar") shouldBe project.layout.buildDirectory.file("ant-bridge/import/jar.properties").get().asFile
    }

    test("an excluded target that supplies properties feeds downstream as an input") {
        val project = project()
        project.write("build.xml", BUILD)
        val replacement = project.tasks.register("compile")
        project.import("build.xml") {
            exclude("compile") { properties(replacement.map { mapOf("classes" to "out") }) }
        }
        val jar = project.tasks.getByName("jar") as AntImportedTarget
        val exports = jar.upstreamExports.get().map { it.asFile }
        exports.size shouldBe 2
        exports[0] shouldBe project.exportOf("init")
        val materialised = project.tasks.single { task -> task.outputs.files.files.contains(exports[1]) }
        materialised.dependencyNames() shouldContain "compile"
        jar.dependencyNames() shouldContain materialised.name
    }

    test("an excluded target in the middle keeps its upstream for downstream") {
        val project = project()
        project.write(
            "build.xml",
            """
            <project name="p">
                <target name="init"/>
                <target name="compile" depends="init"/>
                <target name="jar" depends="compile"/>
            </project>
            """
        )
        project.import("build.xml") { exclude("compile") }
        project.tasks.register("compile")
        val jar = project.tasks.getByName("jar") as AntImportedTarget
        jar.upstreamExports.get().map { it.asFile } shouldBe listOf(project.exportOf("init"))
        jar.dependencyNames() shouldContainAll listOf("init", "compile")
    }

    test("propertiesFor carries upstream task dependencies") {
        val project = project()
        project.write(
            "build.xml",
            """
            <project name="p">
                <property name="top" value="t"/>
                <target name="init"/>
                <target name="compile" depends="init"/>
            </project>
            """
        )
        val imp = project.import("build.xml") {
            properties(mapOf("seed" to "s"))
            exclude("compile")
        }
        val provider: Provider<Map<String, String>> = imp.propertiesFor("compile")
        val consumer = project.tasks.register("consumer") { inputs.property("p", provider) }.get()
        consumer.dependencyNames() shouldContain "init"
        project.exportOf("init").apply { parentFile.mkdirs(); writeText("fromInit=i\ntop=ignored\n") }
        // Reading a task's output before the task ran is refused; mark init as having run.
        (project.tasks.getByName("init") as TaskInternal).state.setOutcome(TaskExecutionOutcome.EXECUTED)
        provider.get() shouldBe mapOf("seed" to "s", "top" to "t", "fromInit" to "i")
    }

    test("seeds are resolved when the build is read and reach the tasks") {
        val project = project()
        project.write("build.xml", """<project name="p"><property name="x" value="${'$'}{s}"/><target name="a"/></project>""")
        val imp = project.import("build.xml") { properties(project.provider { mapOf("s" to "v") }) }
        (project.tasks.getByName("a") as AntImportedTarget).seeds.get() shouldBe mapOf("s" to "v")
        imp.propertiesFor("a").get() shouldBe mapOf("s" to "v", "x" to "v")
    }

    test("excluding an unknown target fails") {
        val project = project()
        project.write("build.xml", BUILD)
        shouldThrow<InvalidUserDataException> { project.import("build.xml") { exclude("nope") } }
            .message shouldContain "nope"
    }

    test("excluding a target with downstream targets and no properties warns") {
        val project = project()
        // Not init: evaluating the root project adds Gradle's own init task.
        project.write(
            "build.xml",
            """
            <project name="p">
                <target name="prepare"/>
                <target name="compile" depends="prepare"/>
                <target name="jar" depends="compile"/>
                <target name="unused"/>
            </project>
            """
        )
        val logger = mockk<Logger>(relaxed = true)
        project.extensions.getByType<AntBridgeExtension>().imports.logger = logger
        val replacement = project.tasks.register("prepare")
        project.import("build.xml") {
            exclude("compile", "unused")
            exclude("prepare") { properties(replacement.map { emptyMap() }) }
        }
        verify(exactly = 0) { logger.warn(any<String>()) }
        (project as ProjectInternal).evaluate()
        verify(exactly = 1) { logger.warn(match<String> { it.contains("'compile'") && it.contains("jar") }) }
        verify(exactly = 0) { logger.warn(match<String> { it.contains("'prepare'") || it.contains("'unused'") }) }
    }

    test("isolation and the import's classpath reach the tasks") {
        val project = project()
        project.write("build.xml", BUILD)
        project.import("build.xml") { isolation(AntIsolation.PROCESS) }
        val jar = project.tasks.getByName("jar") as AntImportedTarget
        jar.isolation.get() shouldBe AntIsolation.PROCESS
        jar.antClasspath.files shouldBe TestAnt.classpath.toSet()
        jar.topLevelBuildFiles.get() shouldBe listOf(project.file("build.xml"))
        jar.baseDirectories.get() shouldBe listOf("")
        jar.buildFiles.files.map { it.canonicalFile } shouldContain project.file("build.xml").canonicalFile
        jar.buildFiles.files shouldNotContain project.file("missing.xml")
    }
})
