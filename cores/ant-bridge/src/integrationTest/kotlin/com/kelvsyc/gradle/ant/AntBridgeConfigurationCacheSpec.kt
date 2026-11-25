package com.kelvsyc.gradle.ant

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

private const val REUSED = "Configuration cache entry reused"

private val KOTLIN_HEADER = """
    import com.kelvsyc.gradle.ant.AntBridgeExtension
    import com.kelvsyc.gradle.ant.AntIsolation
    import com.kelvsyc.gradle.ant.AntOperations
    import com.kelvsyc.gradle.ant.AntWork
    import com.kelvsyc.gradle.ant.antProperty
    import javax.inject.Inject

    apply(plugin = "com.kelvsyc.gradle.ant-bridge")

    repositories { mavenCentral() }

    val antBridge = the<AntBridgeExtension>()
""".trimIndent()

private fun kotlinProject(name: String, body: String, settings: String = ""): File {
    val dir = IntegrationTestSupport.newProjectDir("ant-bridge-$name")
    File(dir, "settings.gradle.kts").writeText(settings)
    File(dir, "build.gradle.kts").writeText(
        IntegrationTestSupport.buildscriptBlock() + "\n\n" + KOTLIN_HEADER + "\n\n" + body.trimIndent() + "\n"
    )
    return dir
}

private fun cc(dir: File, vararg args: String): BuildResult =
    IntegrationTestSupport.run(dir, *args, "--configuration-cache")

private fun File.child(path: String) = File(this, path).apply { parentFile.mkdirs() }

/**
 * Runs the ant-bridge plugin in real builds under the configuration cache. Every build runs twice: the second run
 * must reuse the cache entry and have the same effect.
 */
class AntBridgeConfigurationCacheSpec : FunSpec({
    test("AntOperations from a custom task class") {
        val dir = kotlinProject(
            "custom-task",
            """
            abstract class Legacy @Inject constructor(objects: ObjectFactory) : DefaultTask() {
                private val ant = objects.newInstance(AntOperations::class.java)

                @get:Classpath
                abstract val cp: ConfigurableFileCollection

                @get:OutputDirectory
                abstract val out: DirectoryProperty

                @TaskAction
                fun go() {
                    val marker = out.get().file("marker")
                    if (marker.asFile.exists()) {
                        logger.lifecycle("marker exists")
                    } else {
                        ant.execute(cp) { task("touch", "file" to marker) }
                    }
                }
            }

            tasks.register<Legacy>("legacy") {
                cp.from(antBridge.classpath)
                out.set(layout.buildDirectory.dir("legacy"))
            }
            """
        )

        cc(dir, "legacy").task(":legacy")?.outcome shouldBe TaskOutcome.SUCCESS
        File(dir, "build/legacy/marker").exists() shouldBe true

        val second = cc(dir, "legacy")
        second.output shouldContain REUSED
        second.task(":legacy")?.outcome shouldBe TaskOutcome.UP_TO_DATE
    }

    test("AntOperations from doLast with execution-time branching") {
        val dir = kotlinProject(
            "do-last",
            """
            tasks.register("legacy") {
                // Locals, not script-level vals: a lambda reading a script-level val captures the script object.
                val ops = antBridge.operations
                val cp = antBridge.classpath
                val root = layout.projectDirectory
                doLast {
                    listOf("a", "b").forEach { n ->
                        if (!root.dir(n).asFile.exists()) {
                            ops.execute(cp) {
                                task("echo", "message" to "creating ${'$'}n")
                                task("mkdir", "dir" to root.dir(n))
                            }
                        }
                    }
                }
            }
            """
        )
        File(dir, "a").mkdirs()

        val first = cc(dir, "legacy")
        first.task(":legacy")?.outcome shouldBe TaskOutcome.SUCCESS
        first.output shouldNotContain "creating a"
        Regex("creating b").findAll(first.output).toList() shouldHaveSize 1
        File(dir, "b").isDirectory shouldBe true

        File(dir, "a").deleteRecursively()
        File(dir, "b").deleteRecursively()
        val second = cc(dir, "legacy")
        second.output shouldContain REUSED
        second.output shouldContain "creating a"
        second.output shouldContain "creating b"
        File(dir, "a").isDirectory shouldBe true
        File(dir, "b").isDirectory shouldBe true
    }

    test("Groovy DSL doLast with record") {
        val dir = IntegrationTestSupport.newProjectDir("ant-bridge-groovy")
        File(dir, "settings.gradle").writeText("")
        File(dir, "build.gradle").writeText(
            IntegrationTestSupport.groovyBuildscriptBlock() + "\n\n" +
                """
                apply plugin: 'com.kelvsyc.gradle.ant-bridge'

                repositories { mavenCentral() }

                def ops = antBridge.operations
                def cp = antBridge.classpath
                tasks.register('legacy') {
                    doLast {
                        ops.record(cp) {
                            copy(todir: 'out') {
                                fileset(dir: 'src') {
                                    include(name: '**/*.txt')
                                }
                            }
                        }
                    }
                }
                """.trimIndent()
        )
        dir.child("src/a.txt").writeText("a")
        dir.child("src/b.bin").writeText("b")

        cc(dir, "legacy").task(":legacy")?.outcome shouldBe TaskOutcome.SUCCESS
        File(dir, "out/a.txt").readText() shouldBe "a"
        File(dir, "out/b.bin").exists() shouldBe false

        File(dir, "out").deleteRecursively()
        val second = cc(dir, "legacy")
        second.output shouldContain REUSED
        File(dir, "out/a.txt").readText() shouldBe "a"
    }

    test("Kotlin withGroovyBuilder body moved into AntWork.record") {
        val dir = kotlinProject(
            "with-groovy-builder",
            """
            tasks.register<AntWork>("legacy") {
                record {
                    withGroovyBuilder {
                        "copy"("todir" to "out") {
                            "fileset"("dir" to "src") {
                                "include"("name" to "**/*.txt")
                            }
                        }
                    }
                }
            }
            """
        )
        dir.child("src/a.txt").writeText("a")
        dir.child("src/b.bin").writeText("b")

        cc(dir, "legacy").task(":legacy")?.outcome shouldBe TaskOutcome.SUCCESS
        File(dir, "out/a.txt").readText() shouldBe "a"
        File(dir, "out/b.bin").exists() shouldBe false

        File(dir, "out").deleteRecursively()
        val second = cc(dir, "legacy")
        second.output shouldContain REUSED
        File(dir, "out/a.txt").readText() shouldBe "a"
    }

    test("property loading: expansion, prefix and seeds") {
        val dir = propertyProject("properties")

        val first = cc(dir, "show")
        first.output shouldContain "DIR=/srv/app"
        first.output shouldContain "SHOW=/srv/app"

        val second = cc(dir, "show")
        second.output shouldContain REUSED
        second.output shouldContain "SHOW=/srv/app"
    }

    test("property loading: editing the file invalidates the cache") {
        val dir = propertyProject("properties-edit")

        cc(dir, "show").output shouldContain "DIR=/srv/app"

        File(dir, "build.properties").writeText("dir=${'$'}{b.base}/other\n")
        val second = cc(dir, "show")
        second.output shouldNotContain REUSED
        second.output shouldContain "DIR=/srv/other"
        second.output shouldContain "SHOW=/srv/other"
    }

    test("generic AntWork task executes") {
        val dir = kotlinProject(
            "generic",
            """
            tasks.register<AntWork>("copyText") {
                ant {
                    task("copy", "todir" to layout.projectDirectory.dir("out")) {
                        fileset(layout.projectDirectory.dir("src"), includes = listOf("**/*.txt"))
                    }
                }
            }
            """
        )
        dir.child("src/a.txt").writeText("a")
        dir.child("src/b.bin").writeText("b")

        cc(dir, "copyText").task(":copyText")?.outcome shouldBe TaskOutcome.SUCCESS
        File(dir, "out/a.txt").readText() shouldBe "a"
        File(dir, "out/b.bin").exists() shouldBe false

        File(dir, "out").deleteRecursively()
        val second = cc(dir, "copyText")
        second.output shouldContain REUSED
        File(dir, "out/a.txt").readText() shouldBe "a"
    }

    test("AntWork with absent outputs is never up to date") {
        val dir = kotlinProject(
            "no-outputs",
            """
            tasks.register<AntWork>("hello") {
                ant { task("echo", "message" to "hello") }
            }
            """
        )

        cc(dir, "hello").task(":hello")?.outcome shouldBe TaskOutcome.SUCCESS
        val second = cc(dir, "hello")
        second.output shouldContain REUSED
        second.task(":hello")?.outcome shouldBe TaskOutcome.SUCCESS
        second.output shouldContain "[ant:echo] hello"
    }

    test("taskdef of an antlib from the antBridge configuration") {
        val dir = kotlinProject(
            "antlib",
            """
            dependencies {
                "antBridge"("ant-contrib:ant-contrib:1.0b3") { exclude(group = "ant") }
            }

            tasks.register<AntWork>("contrib") {
                ant {
                    task(
                        "taskdef",
                        "resource" to "net/sf/antcontrib/antlib.xml",
                        "uri" to "antlib:net.sf.antcontrib"
                    )
                    task("var", "name" to "n", "value" to "1", namespace = "antlib:net.sf.antcontrib")
                    task("echo", "message" to "\${'$'}{n}")
                }
            }
            """
        )

        cc(dir, "contrib").output shouldContain "[ant:echo] 1"
        val second = cc(dir, "contrib")
        second.output shouldContain REUSED
        second.output shouldContain "[ant:echo] 1"
    }

    test("a failing Ant task fails the build") {
        val dir = kotlinProject(
            "failing",
            """
            tasks.register<AntWork>("failing") {
                ant { task("fail", "message" to "boom") }
            }
            """
        )

        IntegrationTestSupport.runFailing(dir, "failing", "--configuration-cache").output shouldContain "boom"
        val second = IntegrationTestSupport.runFailing(dir, "failing", "--configuration-cache")
        second.output shouldContain REUSED
        second.output shouldContain "boom"
    }

    test("process isolation") {
        val dir = kotlinProject(
            "process",
            """
            println("DAEMON_PID=${'$'}{ProcessHandle.current().pid()}")

            tasks.register<AntWork>("isolated") {
                isolation.set(AntIsolation.PROCESS)
                ant { task("echo", "message" to "isolated") }
            }
            """
        )

        val first = cc(dir, "isolated", "--info")
        first.task(":isolated")?.outcome shouldBe TaskOutcome.SUCCESS
        val daemonPid = Regex("DAEMON_PID=(\\d+)").find(first.output)?.groupValues?.get(1)
        val workerPid = Regex("Running Ant .+ in process (\\d+)").find(first.output)?.groupValues?.get(1)
        daemonPid shouldNotBe null
        workerPid shouldNotBe null
        workerPid shouldNotBe daemonPid

        val second = cc(dir, "isolated", "--info")
        second.output shouldContain REUSED
        second.task(":isolated")?.outcome shouldBe TaskOutcome.SUCCESS
        val secondWorkerPid = Regex("Running Ant .+ in process (\\d+)").find(second.output)?.groupValues?.get(1)
        secondWorkerPid shouldNotBe null
        secondWorkerPid shouldNotBe daemonPid
    }

    test("the bridge's Ant version runs, not Gradle's") {
        val dir = kotlinProject(
            "ant-version",
            """
            dependencies {
                "antBridge"("org.apache.ant:ant") { version { strictly("1.10.14") } }
                "antBridge"("org.apache.ant:ant-junit") { version { strictly("1.10.14") } }
            }

            tasks.register<AntWork>("version") {
                ant { task("echo", "message" to "VERSION=\${'$'}{ant.version}") }
            }
            """
        )

        cc(dir, "version").output shouldContain Regex("VERSION=.*1\\.10\\.14")
        val second = cc(dir, "version")
        second.output shouldContain REUSED
        second.output shouldContain Regex("VERSION=.*1\\.10\\.14")
    }

    test("parallel AntWork tasks do not share properties") {
        val dir = kotlinProject(
            "parallel",
            """
            subprojects {
                apply(plugin = "com.kelvsyc.gradle.ant-bridge")
                repositories { mavenCentral() }
                val projectName = name
                tasks.register<AntWork>("echoName") {
                    ant {
                        task("property", "name" to "x", "value" to projectName)
                        task("echo", "message" to "NAME=\${'$'}{x}")
                    }
                }
            }
            """,
            settings = """include("p1", "p2")"""
        )
        File(dir, "p1").mkdirs()
        File(dir, "p2").mkdirs()

        listOf(cc(dir, "echoName", "--parallel"), cc(dir, "echoName", "--parallel")).forEach { result ->
            result.output shouldContain "NAME=p1"
            result.output shouldContain "NAME=p2"
            result.task(":p1:echoName")?.outcome shouldBe TaskOutcome.SUCCESS
            result.task(":p2:echoName")?.outcome shouldBe TaskOutcome.SUCCESS
        }
    }

    test("paths with spaces and non-ASCII") {
        val dir = kotlinProject(
            "paths",
            """
            tasks.register<AntWork>("copyText") {
                ant {
                    task("copy", "todir" to layout.projectDirectory.dir("out")) {
                        fileset(layout.projectDirectory.dir("src dir/é"))
                    }
                }
            }
            """
        )
        dir.child("src dir/é/für.txt").writeText("x")

        cc(dir, "copyText").task(":copyText")?.outcome shouldBe TaskOutcome.SUCCESS
        File(dir, "out/für.txt").readText() shouldBe "x"

        File(dir, "out").deleteRecursively()
        val second = cc(dir, "copyText")
        second.output shouldContain REUSED
        File(dir, "out/für.txt").readText() shouldBe "x"
    }
})

private fun propertyProject(name: String): File {
    val dir = kotlinProject(
        name,
        """
        val p = antBridge.properties {
            file("build.properties") { prefix.set("b") }
            seed("b.base", "/srv")
        }
        println("DIR=" + p.antProperty("b.dir").get())

        tasks.register("show") {
            val dirValue = p.antProperty("b.dir")
            doLast { println("SHOW=" + dirValue.get()) }
        }
        """
    )
    File(dir, "build.properties").writeText("dir=${'$'}{b.base}/app\n")
    return dir
}
