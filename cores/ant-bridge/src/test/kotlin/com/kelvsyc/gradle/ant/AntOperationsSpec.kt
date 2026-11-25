package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntWorkAction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

class AntOperationsSpec : FunSpec({
    class Fixture {
        val project: Project = ProjectBuilder.builder().build()
        val workers = mockk<WorkerExecutor>()
        val queue = mockk<WorkQueue>()
        val parameters = slot<Action<in AntWorkAction.Parameters>>()
        val operations = object : AntOperations(workers, project.layout) {}

        init {
            every { workers.classLoaderIsolation(any()) } returns queue
            every { workers.processIsolation(any()) } returns queue
            every { workers.await() } just runs
            every { queue.submit(AntWorkAction::class.java, capture(parameters)) } just runs
        }
    }

    test("classloader isolation by default") {
        val fixture = Fixture()
        fixture.operations.execute(fixture.project.files()) { task("echo", "message" to "hi") }
        verify { fixture.workers.classLoaderIsolation(any()) }
        verify(exactly = 0) { fixture.workers.processIsolation(any()) }
        verify { fixture.workers.await() }
    }

    test("process isolation when selected") {
        val fixture = Fixture()
        fixture.operations.record(fixture.project.files(), AntIsolation.PROCESS) {
            invokeMethod("echo", arrayOf(mapOf("message" to "hi")))
        }
        verify { fixture.workers.processIsolation(any()) }
        verify(exactly = 0) { fixture.workers.classLoaderIsolation(any()) }
        verify { fixture.workers.await() }
    }

    test("submitted nodes are resolved") {
        val fixture = Fixture()
        val project = fixture.project
        val dir = project.layout.buildDirectory.dir("out")
        fixture.operations.execute(project.files("ant.jar")) { task("mkdir", "dir" to dir) }

        val params = project.objects.newInstance(AntWorkAction.Parameters::class.java)
        fixture.parameters.captured.execute(params)
        val nodes = params.nodes.get()
        nodes.map { it.name } shouldContainExactly listOf("mkdir")
        nodes.single().attributes shouldBe mapOf("dir" to dir.get().asFile.absolutePath)
        params.baseDirectory.get().asFile shouldBe project.projectDir
        params.antClasspath.files shouldBe setOf(project.file("ant.jar"))
    }
})
