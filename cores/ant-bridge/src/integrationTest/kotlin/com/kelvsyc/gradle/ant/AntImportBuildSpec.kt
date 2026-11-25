package com.kelvsyc.gradle.ant

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

private const val REUSED = "Configuration cache entry reused"

private val IMPORT_HEADER = """
    import com.kelvsyc.gradle.ant.AntBridgeExtension
    import com.kelvsyc.gradle.ant.AntImport
    import com.kelvsyc.gradle.ant.AntIsolation
    import com.kelvsyc.gradle.providers.getting

    apply(plugin = "com.kelvsyc.gradle.ant-bridge")

    repositories { mavenCentral() }

    val antBridge = the<AntBridgeExtension>()
""".trimIndent()

private fun importProject(name: String, body: String): File {
    val dir = IntegrationTestSupport.newProjectDir("ant-import-$name")
    File(dir, "settings.gradle.kts").writeText("")
    File(dir, "build.gradle.kts").writeText(
        IntegrationTestSupport.buildscriptBlock() + "\n\n" + IMPORT_HEADER + "\n\n" + body.trimIndent() + "\n"
    )
    return dir
}

private fun cc(dir: File, vararg args: String): BuildResult =
    IntegrationTestSupport.run(dir, *args, "--configuration-cache")

/** Writes an Ant file, with `%{` standing for Ant's `${` so that fixtures need no Kotlin escaping. */
private fun File.ant(path: String, xml: String) {
    val file = File(this, path)
    file.parentFile.mkdirs()
    file.writeText(xml.trimIndent().replace("%{", "\${") + "\n")
}

private fun BuildResult.count(text: String) = Regex(Regex.escape(text)).findAll(output).count()

private fun BuildResult.value(key: String): String =
    requireNotNull(Regex("(?<![\\w.-])$key=(.+)").find(output)?.groupValues?.get(1)?.trim()) { "$key not in output" }

private fun BuildResult.outcomes(vararg paths: String) = paths.map { task(it)?.outcome }

private const val CHAIN = """
    <project name="chain">
        <target name="setup"><echo message="SETUP-RAN"/></target>
        <target name="compile" depends="setup"><echo message="COMPILE-RAN"/></target>
        <target name="jar" depends="compile"><echo message="JAR-RAN"/></target>
    </project>
"""

private const val EXCLUDABLE = """
    <project name="excludable">
        <target name="setup"><property name="src.dir" value="SRC-FROM-SETUP"/></target>
        <target name="compile" depends="setup"><property name="build.dir" value="ant-build"/></target>
        <target name="jar" depends="compile">
            <echo message="BUILD-DIR=%{build.dir}"/>
            <echo message="SRC=%{src.dir}"/>
        </target>
    </project>
"""

/**
 * Runs `antBridge.importBuild` in real builds under the configuration cache. Unless a test says otherwise, every
 * build runs twice and the second run must reuse the cache entry with the same effect.
 */
class AntImportBuildSpec : FunSpec({
    test("running a downstream target runs its upstream targets as tasks") {
        val dir = importProject("chain", """antBridge.importBuild("build.xml")""")
        dir.ant("build.xml", CHAIN)

        listOf(cc(dir, "jar"), cc(dir, "jar")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.outcomes(":setup", ":compile", ":jar") shouldContainExactly List(3) { TaskOutcome.SUCCESS }
            val positions = listOf("SETUP-RAN", "COMPILE-RAN", "JAR-RAN").map { result.output.indexOf(it) }
            positions.forEach { it shouldNotBe -1 }
            positions shouldBe positions.sorted()
        }
    }

    test("depends order is kept between the tasks it names") {
        val dir = importProject("depends-order", """antBridge.importBuild("build.xml")""")
        dir.ant(
            "build.xml",
            """
            <project name="order">
                <target name="clean"><delete dir="out"/></target>
                <target name="build"><mkdir dir="out"/><touch file="out/built.txt"/></target>
                <target name="rebuild" depends="clean,build"/>
            </project>
            """
        )

        listOf(cc(dir, "rebuild"), cc(dir, "rebuild")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.outcomes(":clean", ":build", ":rebuild") shouldContainExactly List(3) { TaskOutcome.SUCCESS }
            (result.output.indexOf("> Task :clean") < result.output.indexOf("> Task :build")) shouldBe true
            File(dir, "out/built.txt").isFile shouldBe true
        }
    }

    test("properties flow between targets") {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val dir = importProject("flow", """antBridge.importBuild("build.xml")""")
        dir.ant(
            "build.xml",
            """
            <project name="flow">
                <target name="setup">
                    <property name="p.prop" value="from-property"/>
                    <available file="build.xml" property="p.avail"/>
                    <exec executable="$java" outputproperty="p.out" errorproperty="p.err" failonerror="true">
                        <arg value="-version"/>
                    </exec>
                </target>
                <target name="jar" depends="setup">
                    <echo message="PROP=%{p.prop}"/>
                    <echo message="AVAIL=%{p.avail}"/>
                    <echo message="ERR=%{p.err}"/>
                </target>
            </project>
            """
        )

        listOf(cc(dir, "jar"), cc(dir, "jar")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.value("PROP") shouldBe "from-property"
            result.value("AVAIL") shouldBe "true"
            result.value("ERR") shouldContain "version"
        }
    }

    test("unrelated targets don't see each other's properties") {
        val dir = importProject(
            "unrelated",
            """
            antBridge.importBuild("build.xml")
            tasks.named("b") { mustRunAfter("a") }
            """
        )
        dir.ant(
            "build.xml",
            """
            <project name="unrelated">
                <target name="a"><property name="x" value="from-a"/></target>
                <target name="b"><echo message="X=%{x}"/></target>
            </project>
            """
        )

        listOf(cc(dir, "a", "b"), cc(dir, "a", "b")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.outcomes(":a", ":b") shouldContainExactly List(2) { TaskOutcome.SUCCESS }
            (result.output.indexOf("> Task :a") < result.output.indexOf("> Task :b")) shouldBe true
            result.value("X") shouldBe "\${x}"
        }
    }

    test("if/unless uses upstream values") {
        val dir = importProject("if-unless", """antBridge.importBuild("build.xml")""")
        dir.ant(
            "build.xml",
            """
            <project name="conditions">
                <target name="setup"><property name="enabled" value="true"/></target>
                <target name="on" depends="setup" if="enabled"><echo message="ON-RAN"/></target>
                <target name="off" depends="setup" unless="enabled"><echo message="OFF-RAN"/></target>
                <target name="detached" if="enabled"><echo message="DETACHED-RAN"/></target>
            </project>
            """
        )

        listOf(cc(dir, "on", "off", "detached"), cc(dir, "on", "off", "detached")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.output shouldContain "ON-RAN"
            result.output shouldNotContain "OFF-RAN"
            result.output shouldNotContain "DETACHED-RAN"
        }
    }

    test("two files: cross-file depends, top-level properties and base directories") {
        val dir = importProject(
            "two-files",
            """
            antBridge.importBuild("a/build.xml")
            antBridge.importBuild("b/build.xml", "b-base") {}
            """
        )
        File(dir, "b-base").mkdirs()
        dir.ant(
            "a/build.xml",
            """
            <project name="a">
                <property name="a.top" value="A-TOP"/>
                <target name="aprep">
                    <echo message="APREP-BASEDIR=%{basedir}"/>
                    <property name="from.a" value="FROM-A"/>
                </target>
            </project>
            """
        )
        dir.ant(
            "b/build.xml",
            """
            <project name="b">
                <property name="b.top" value="B-TOP"/>
                <target name="bjar" depends="aprep">
                    <echo message="BJAR-BASEDIR=%{basedir}"/>
                    <echo message="VALUES=%{a.top},%{b.top},%{from.a}"/>
                </target>
            </project>
            """
        )

        listOf(cc(dir, "bjar"), cc(dir, "bjar")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.outcomes(":aprep", ":bjar") shouldContainExactly List(2) { TaskOutcome.SUCCESS }
            File(result.value("APREP-BASEDIR")).canonicalPath shouldBe File(dir, "a").canonicalPath
            File(result.value("BJAR-BASEDIR")).canonicalPath shouldBe File(dir, "b-base").canonicalPath
            result.value("VALUES") shouldBe "A-TOP,B-TOP,FROM-A"
        }
    }

    test("exclude with a native replacement") {
        val dir = importProject(
            "exclude",
            """
            lateinit var imported: AntImport

            val replacement = tasks.register("compile") {
                val src = imported.propertiesFor("compile").getting("src.dir")
                inputs.property("src", src)
                doLast { println("REPLACEMENT-SRC=" + src.get()) }
            }

            imported = antBridge.importBuild("build.xml") {
                exclude("compile") { properties(replacement.map { mapOf("build.dir" to "NATIVE-BUILD") }) }
            }
            """
        )
        dir.ant("build.xml", EXCLUDABLE)

        listOf(cc(dir, "jar"), cc(dir, "jar")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.outcomes(":setup", ":compile", ":jar") shouldContainExactly List(3) { TaskOutcome.SUCCESS }
            result.output.indexOf("> Task :setup") shouldNotBe -1
            (result.output.indexOf("> Task :setup") < result.output.indexOf("> Task :compile")) shouldBe true
            result.value("REPLACEMENT-SRC") shouldBe "SRC-FROM-SETUP"
            result.value("BUILD-DIR") shouldBe "NATIVE-BUILD"
            result.value("SRC") shouldBe "SRC-FROM-SETUP"
        }
    }

    test("excluding without properties warns") {
        val dir = importProject(
            "exclude-warn",
            """
            tasks.register("compile")
            antBridge.importBuild("build.xml") { exclude("compile") }
            """
        )
        dir.ant("build.xml", EXCLUDABLE)

        val first = cc(dir, "jar")
        first.output shouldContain "Excluded Ant target 'compile' has imported targets downstream (jar)"
        first.value("BUILD-DIR") shouldBe "\${build.dir}"

        val second = cc(dir, "jar")
        second.output shouldContain REUSED
        second.value("BUILD-DIR") shouldBe "\${build.dir}"
    }

    test("seeds override file defaults") {
        val dir = importProject(
            "seeds",
            """
            antBridge.importBuild("build.xml") {
                properties(mapOf("greeting" to "seeded"))
                properties(providers.provider { mapOf("other" to "from-provider", "greeting" to "ignored") })
            }
            """
        )
        dir.ant(
            "build.xml",
            """
            <project name="seeds">
                <property name="greeting" value="file-default"/>
                <property name="other" value="file-other"/>
                <target name="show"><echo message="VALUES=%{greeting},%{other}"/></target>
            </project>
            """
        )

        listOf(cc(dir, "show"), cc(dir, "show")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.value("VALUES") shouldBe "seeded,from-provider"
        }
    }

    test("a failing target fails the build with Ant's message and no stale export") {
        val dir = importProject("failing", """antBridge.importBuild("build.xml")""")
        val fixture = """
            <project name="failing">
                <target name="setup"><property name="v" value="ok"/>FAILURE</target>
                <target name="jar" depends="setup"><echo message="V=%{v}"/></target>
            </project>
        """
        dir.ant("build.xml", fixture.replace("FAILURE", ""))
        val export = File(dir, "build/ant-bridge/import/setup.properties")

        listOf(cc(dir, "jar"), cc(dir, "jar")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.value("V") shouldBe "ok"
        }
        export.isFile shouldBe true

        dir.ant("build.xml", fixture.replace("FAILURE", """<fail message="BOOM-FROM-ANT"/>"""))
        val failed = IntegrationTestSupport.runFailing(dir, "jar", "--configuration-cache")
        failed.output shouldContain "BOOM-FROM-ANT"
        failed.task(":setup")?.outcome shouldBe TaskOutcome.FAILED
        failed.task(":jar") shouldBe null
        export.exists() shouldBe false
    }

    test("editing an imported file invalidates the cache, and a cache hit does not re-run top-level side effects") {
        val dir = importProject("invalidate", """antBridge.importBuild("build.xml")""")
        dir.ant(
            "build.xml",
            """
            <project name="main">
                <import file="common.xml"/>
                <echo message="TOP-LEVEL"/>
                <target name="jar" depends="setup"><echo message="JAR-RAN"/></target>
            </project>
            """
        )
        val common = """
            <project name="common">
                <target name="setup"><echo message="MESSAGE"/></target>
            </project>
        """
        dir.ant("common.xml", common.replace("MESSAGE", "SETUP-ONE"))

        val first = cc(dir, "jar")
        first.count("TOP-LEVEL") shouldBeGreaterThanOrEqual 1
        first.output shouldContain "SETUP-ONE"

        val second = cc(dir, "jar")
        second.output shouldContain REUSED
        val executed = second.tasks.count { it.outcome == TaskOutcome.SUCCESS }
        executed shouldBe 2
        second.count("TOP-LEVEL") shouldBe executed

        dir.ant("common.xml", common.replace("MESSAGE", "SETUP-TWO"))
        val third = cc(dir, "jar")
        third.output shouldNotContain REUSED
        third.output shouldContain "SETUP-TWO"
    }

    test("editing a property file read at the top level invalidates the cache") {
        val dir = importProject(
            "property-file",
            """
            val imported = antBridge.importBuild("build.xml")

            tasks.register("printer") {
                val version = imported.propertiesFor("show").getting("version")
                inputs.property("version", version)
                doLast { println("PRINTED=" + version.get()) }
            }
            """
        )
        dir.ant(
            "build.xml",
            """
            <project name="props">
                <property file="build.properties"/>
                <target name="show"><echo message="SHOWN=%{version}"/></target>
            </project>
            """
        )
        File(dir, "build.properties").writeText("version=1.0\n")

        listOf(cc(dir, "show", "printer"), cc(dir, "show", "printer")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            result.value("SHOWN") shouldBe "1.0"
            result.value("PRINTED") shouldBe "1.0"
        }

        File(dir, "build.properties").writeText("version=2.0\n")
        val edited = cc(dir, "show", "printer")
        edited.output shouldNotContain REUSED
        edited.value("SHOWN") shouldBe "2.0"
        edited.value("PRINTED") shouldBe "2.0"
    }

    test("creating a missing property file read at the top level invalidates the cache") {
        val dir = importProject("missing-property-file", """antBridge.importBuild("build.xml")""")
        dir.ant(
            "build.xml",
            """
            <project name="local">
                <property file="local.properties"/>
                <property name="version" value="default"/>
                <target name="show"><echo message="SHOWN=%{version}"/></target>
            </project>
            """
        )

        val first = cc(dir, "show")
        first.value("SHOWN") shouldBe "default"

        File(dir, "local.properties").writeText("version=local\n")
        val second = cc(dir, "show")
        second.output shouldNotContain REUSED
        second.value("SHOWN") shouldBe "local"
    }

    test("process isolation") {
        val dir = importProject(
            "process",
            """
            println("DAEMON_PID=${'$'}{ProcessHandle.current().pid()}")

            antBridge.importBuild("build.xml") { isolation(AntIsolation.PROCESS) }
            """
        )
        dir.ant("build.xml", CHAIN)
        val workerPid = Regex("Running Ant target \\S+ in process (\\d+)")

        val first = cc(dir, "jar", "--info")
        first.outcomes(":setup", ":compile", ":jar") shouldContainExactly List(3) { TaskOutcome.SUCCESS }
        first.output shouldContain "JAR-RAN"
        val daemonPid = first.value("DAEMON_PID")
        val firstWorker = workerPid.find(first.output)?.groupValues?.get(1)
        firstWorker shouldNotBe null
        firstWorker shouldNotBe daemonPid

        val second = cc(dir, "jar", "--info")
        second.output shouldContain REUSED
        second.output shouldContain "JAR-RAN"
        val secondWorker = workerPid.find(second.output)?.groupValues?.get(1)
        secondWorker shouldNotBe null
        secondWorker shouldNotBe daemonPid
    }

    test("paths with spaces") {
        val dir = importProject(
            "spaces",
            """
            antBridge.importBuild("my build/build.xml")
            antBridge.importBuild("my build/other.xml", "base dir") {}
            """
        )
        File(dir, "base dir").mkdirs()
        dir.ant(
            "my build/build.xml",
            """
            <project name="spaces">
                <target name="show"><echo message="BASEDIR=%{basedir}"/></target>
            </project>
            """
        )
        dir.ant(
            "my build/other.xml",
            """
            <project name="other">
                <target name="showother" depends="show"><echo message="OTHER-BASEDIR=%{basedir}"/></target>
            </project>
            """
        )

        listOf(cc(dir, "showother"), cc(dir, "showother")).forEachIndexed { run, result ->
            if (run > 0) result.output shouldContain REUSED
            File(result.value("BASEDIR")).canonicalPath shouldBe File(dir, "my build").canonicalPath
            File(result.value("OTHER-BASEDIR")).canonicalPath shouldBe File(dir, "base dir").canonicalPath
        }
    }
})
