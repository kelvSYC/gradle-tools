package com.kelvsyc.gradle.ant.actions

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AntWorkActionSpec : FunSpec({
    fun action(nodes: List<AntNode>, baseDir: File): AntWorkAction {
        val project = ProjectBuilder.builder().build()
        val params = project.objects.newInstance(AntWorkAction.Parameters::class.java)
        params.nodes.set(nodes)
        params.antClasspath.from(TestAnt.classpath)
        params.baseDirectory.set(baseDir)
        return object : AntWorkAction() {
            override fun getParameters() = params
        }
    }

    test("executes the nodes against the base directory") {
        val baseDir = tempdir()
        action(listOf(AntNode("mkdir", null, mapOf("dir" to "made"), null, emptyList())), baseDir).execute()
        baseDir.resolve("made").isDirectory.shouldBeTrue()
    }

    test("failure propagates as AntExecutionException") {
        val baseDir = tempdir()
        val failing = AntNode("fail", null, mapOf("message" to "boom"), null, emptyList())
        val e = shouldThrow<AntExecutionException> { action(listOf(failing), baseDir).execute() }
        e.message shouldContain "boom"
    }
})
