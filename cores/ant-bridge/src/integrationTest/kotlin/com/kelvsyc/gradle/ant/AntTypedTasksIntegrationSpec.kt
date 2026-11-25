package com.kelvsyc.gradle.ant

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.security.MessageDigest
import java.util.HexFormat
import java.util.concurrent.TimeUnit
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

private const val REUSED = "Configuration cache entry reused"
private const val TOOL_TIMEOUT_SECONDS = 60L

private fun typedProject(name: String, body: String): File {
    val dir = IntegrationTestSupport.newProjectDir("ant-typed-$name")
    File(dir, "settings.gradle.kts").writeText("")
    File(dir, "build.gradle.kts").writeText(
        IntegrationTestSupport.buildscriptBlock() + "\n\n" +
            """
            import com.kelvsyc.gradle.ant.tasks.AntChecksum
            import com.kelvsyc.gradle.ant.tasks.AntJUnitReport
            import com.kelvsyc.gradle.ant.tasks.AntReplaceRegexp
            import com.kelvsyc.gradle.ant.tasks.AntSignJar
            import com.kelvsyc.gradle.ant.tasks.AntXslt
            import com.kelvsyc.gradle.clients.CredentialReference

            apply(plugin = "com.kelvsyc.gradle.ant-bridge")

            repositories { mavenCentral() }
            """.trimIndent() + "\n\n" + body.trimIndent() + "\n"
    )
    return dir
}

private fun runTwice(
    dir: File,
    task: String,
    environment: Map<String, String> = emptyMap()
): Pair<BuildResult, BuildResult> {
    val first = IntegrationTestSupport.run(dir, task, "--configuration-cache", environment = environment)
    first.task(":$task")?.outcome shouldBe TaskOutcome.SUCCESS
    val second = IntegrationTestSupport.run(dir, task, "--configuration-cache", environment = environment)
    second.output shouldContain REUSED
    second.task(":$task")?.outcome shouldBe TaskOutcome.UP_TO_DATE
    return first to second
}

private fun javaTool(name: String): File = File(System.getProperty("java.home"), "bin/$name")

private fun runTool(vararg command: String): Int {
    val process = ProcessBuilder(*command).inheritIO().start()
    val finished = process.waitFor(TOOL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    if (!finished) process.destroyForcibly()
    check(finished) { "${command.first()} did not finish within $TOOL_TIMEOUT_SECONDS seconds" }
    return process.exitValue()
}

private fun File.child(path: String) = File(this, path).apply { parentFile.mkdirs() }

/**
 * Runs each typed Ant task against the real Ant under the configuration cache; the second run is up to date.
 */
class AntTypedTasksIntegrationSpec : FunSpec({
    test("AntReplaceRegexp rewrites a copy and leaves the source unchanged") {
        val dir = typedProject(
            "replaceregexp",
            """
            tasks.register<AntReplaceRegexp>("replace") {
                sourceDirectory.set(layout.projectDirectory.dir("src"))
                match.set("world")
                replace.set("there")
                outputDirectory.set(layout.buildDirectory.dir("replaced"))
            }
            """
        )
        dir.child("src/a.txt").writeText("hello world")

        runTwice(dir, "replace")
        File(dir, "src/a.txt").readText() shouldBe "hello world"
        File(dir, "build/replaced/a.txt").readText() shouldBe "hello there"
    }

    test("AntXslt transforms with a stylesheet parameter") {
        val dir = typedProject(
            "xslt",
            """
            tasks.register<AntXslt>("transform") {
                sourceDirectory.set(layout.projectDirectory.dir("src"))
                stylesheet.set(layout.projectDirectory.file("identity.xsl"))
                parameters.put("greeting", "hi")
                outputDirectory.set(layout.buildDirectory.dir("xslt"))
            }
            """
        )
        dir.child("src/in.xml").writeText("<doc><item>x</item></doc>")
        dir.child("identity.xsl").writeText(
            """
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
              <xsl:param name="greeting"/>
              <xsl:template match="/">
                <out greeting="{${'$'}greeting}"><xsl:copy-of select="."/></out>
              </xsl:template>
            </xsl:stylesheet>
            """.trimIndent()
        )

        runTwice(dir, "transform")
        val output = File(dir, "build/xslt/in.html").readText()
        output shouldContain "greeting=\"hi\""
        output shouldContain "<doc><item>x</item></doc>"
    }

    test("AntChecksum writes the SHA-256 of the input") {
        val dir = typedProject(
            "checksum",
            """
            tasks.register<AntChecksum>("checksum") {
                files.from("input.bin")
                outputDirectory.set(layout.buildDirectory.dir("sums"))
            }
            """
        )
        val bytes = ByteArray(Byte.MAX_VALUE.toInt()) { it.toByte() }
        File(dir, "input.bin").writeBytes(bytes)

        runTwice(dir, "checksum")
        val expected = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
        File(dir, "build/sums/input.bin.sha256").readText().trim() shouldBe expected
    }

    test("AntSignJar signs a jar verifiable by jarsigner")
        .config(enabled = javaTool("keytool").canExecute() && javaTool("jarsigner").canExecute()) {
            val dir = typedProject(
                "signjar",
                """
                tasks.register<AntSignJar>("sign") {
                    jars.from("app.jar")
                    keystore.set(layout.projectDirectory.file("keystore.p12"))
                    alias.set("test")
                    storeType.set("PKCS12")
                    storePassword.set(CredentialReference.EnvironmentVariable("ANT_BRIDGE_STORE_PASSWORD"))
                    outputDirectory.set(layout.buildDirectory.dir("signed"))
                }
                """
            )
            val password = "changeit-123"
            JarOutputStream(File(dir, "app.jar").outputStream()).use { jar ->
                jar.putNextEntry(JarEntry("hello.txt"))
                jar.write("hello".toByteArray())
                jar.closeEntry()
            }
            runTool(
                javaTool("keytool").path, "-genkeypair", "-alias", "test", "-keyalg", "RSA", "-dname", "CN=test",
                "-validity", "1", "-storetype", "PKCS12", "-keystore", File(dir, "keystore.p12").path,
                "-storepass", password, "-keypass", password
            ) shouldBe 0

            runTwice(dir, "sign", environment = mapOf("ANT_BRIDGE_STORE_PASSWORD" to password))
            runTool(javaTool("jarsigner").path, "-verify", File(dir, "build/signed/app.jar").path) shouldBe 0
        }

    test("AntJUnitReport renders a noframes report") {
        val dir = typedProject(
            "junitreport",
            """
            tasks.register<AntJUnitReport>("report") {
                resultsDirectory.set(layout.projectDirectory.dir("results"))
                outputDirectory.set(layout.buildDirectory.dir("report"))
            }
            """
        )
        dir.child("results/TEST-x.xml").writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <testsuite name="x" tests="1" failures="0" errors="0" skipped="0" time="0.1" timestamp="2026-11-24T00:00:00"
                hostname="localhost">
              <properties/>
              <testcase classname="x" name="passes" time="0.1"/>
              <system-out/>
              <system-err/>
            </testsuite>
            """.trimIndent()
        )

        runTwice(dir, "report")
        File(dir, "build/report/junit-noframes.html").readText() shouldContain "passes"
    }
})
