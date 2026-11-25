package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import com.kelvsyc.gradle.ant.AntIsolation
import com.kelvsyc.gradle.clients.CredentialReference
import com.kelvsyc.gradle.plugins.AntBridgePlugin
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AntTypedTasksSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().build()

    fun Project.dir(name: String): File = file(name).also { it.mkdirs() }

    fun AntElement.resolved() = resolve()

    test("AntReplaceRegexp builds the expected Ant element") {
        val project = project()
        val task = project.tasks.register("regexp", AntReplaceRegexp::class.java).get()
        task.sourceDirectory.set(project.dir("src"))
        task.outputDirectory.set(project.dir("out"))
        task.match.set("a(b)")
        task.replace.set("\\1")
        task.excludes.add("**/skip")
        task.encoding.set("UTF-8")
        val element = task.buildElements().single().resolved()
        element.name shouldBe "replaceregexp"
        element.attributes shouldBe mapOf(
            "match" to "a(b)", "replace" to "\\1", "flags" to "", "byline" to "false", "encoding" to "UTF-8"
        )
        val fileset = element.children.single()
        fileset.name shouldBe "fileset"
        fileset.attributes shouldBe mapOf(
            "dir" to project.file("out").absolutePath, "includes" to "**/*", "excludes" to "**/skip"
        )
    }

    test("AntReplaceRegexp does not overlap input and output") {
        val project = project()
        val task = project.tasks.register("regexp", AntReplaceRegexp::class.java).get()
        task.sourceDirectory.set(project.dir("src"))
        task.outputDirectory.set(project.dir("out"))
        task.outputDirectory.get().asFile shouldNotBe task.sourceDirectory.get().asFile
        task.outputs.hasOutput shouldBe true
    }

    test("AntXslt builds the expected Ant element") {
        val project = project()
        val task = project.tasks.register("xslt", AntXslt::class.java).get()
        task.sourceDirectory.set(project.dir("xml"))
        task.outputDirectory.set(project.dir("html"))
        task.stylesheet.set(project.file("style.xsl"))
        task.parameters.put("title", "Hi")
        val element = task.buildElements().single().resolved()
        element.name shouldBe "xslt"
        element.attributes shouldBe mapOf(
            "basedir" to project.file("xml").absolutePath,
            "destdir" to project.file("html").absolutePath,
            "style" to project.file("style.xsl").absolutePath,
            "force" to "true",
            "extension" to ".html",
            "includes" to "**/*"
        )
        element.children.single().name shouldBe "param"
        element.children.single().attributes shouldBe mapOf("name" to "title", "expression" to "Hi")
    }

    test("AntChecksum builds one Ant element per file") {
        val project = project()
        val task = project.tasks.register("sums", AntChecksum::class.java).get()
        task.files.from(project.file("a.jar"), project.file("sub/b.jar"))
        task.outputDirectory.set(project.dir("sums"))
        val elements = task.buildElements().map { it.resolved() }
        elements.map { it.name } shouldContainExactly listOf("checksum", "checksum")
        elements.map { it.attributes["file"] } shouldContainExactly
            listOf(project.file("a.jar").absolutePath, project.file("sub/b.jar").absolutePath)
        elements.first().attributes.filterKeys { it != "file" } shouldBe mapOf(
            "todir" to project.file("sums").absolutePath, "algorithm" to "SHA-256", "fileext" to ".sha256",
            "forceoverwrite" to "true"
        )
    }

    test("AntSignJar builds the expected Ant element") {
        val project = project()
        val task = project.tasks.register("sign", AntSignJar::class.java).get()
        task.jars.from(project.file("a.jar"))
        task.keystore.set(project.file("ks.p12"))
        task.alias.set("me")
        task.storePassword.set(CredentialReference.Literal("store"))
        task.keyPassword.set(CredentialReference.Literal("key"))
        task.tsaUrl.set("http://tsa")
        task.outputDirectory.set(project.dir("signed"))
        val element = task.buildElements().single().resolved()
        element.name shouldBe "signjar"
        element.attributes shouldBe mapOf(
            "jar" to project.file("a.jar").absolutePath,
            "destDir" to project.file("signed").absolutePath,
            "keystore" to project.file("ks.p12").absolutePath,
            "alias" to "me",
            "storepass" to "store",
            "keypass" to "key",
            "tsaurl" to "http://tsa",
            "force" to "true"
        )
    }

    test("AntSignJar keeps passwords out of inputs") {
        val project = project()
        val task = project.tasks.register("sign", AntSignJar::class.java).get()
        task.alias.set("me")
        task.storePassword.set(CredentialReference.Literal("store"))
        task.inputs.properties.keys shouldNotContain "storePassword"
        task.inputs.properties.keys shouldNotContain "keyPassword"
        task.inputs.properties.keys shouldContain "alias"
    }

    test("AntJUnitReport builds the expected Ant element") {
        val project = project()
        val task = project.tasks.register("report", AntJUnitReport::class.java).get()
        task.resultsDirectory.set(project.dir("results"))
        task.outputDirectory.set(project.dir("report"))
        val element = task.buildElements().single().resolved()
        element.name shouldBe "junitreport"
        element.attributes shouldBe mapOf("todir" to task.temporaryDir.absolutePath)
        element.children.map { it.name } shouldContainExactly listOf("fileset", "report")
        element.children[0].attributes shouldBe
            mapOf("dir" to project.file("results").absolutePath, "includes" to "TEST-*.xml")
        element.children[1].attributes shouldBe
            mapOf("format" to "noframes", "todir" to project.file("report").absolutePath)
    }

    fun Task.runActions() = actions.forEach { it.execute(this) }

    test("an output directory holding the task's inputs fails before anything is deleted") {
        val project = project()
        val source = project.dir("src")
        File(source, "a.xml").writeText("<a/>")
        val xslt = project.tasks.register("xslt", AntXslt::class.java).get()
        xslt.sourceDirectory.set(source)
        xslt.stylesheet.set(project.file("style.xsl").also { it.writeText("<x/>") })
        xslt.outputDirectory.set(source)
        shouldThrow<InvalidUserDataException> { xslt.runActions() }.message shouldContain "a.xml"
        File(source, "a.xml").exists() shouldBe true

        val jars = project.dir("jars")
        val jar = File(jars, "lib.jar").also { it.writeText("not a jar") }
        val sign = project.tasks.register("sign", AntSignJar::class.java).get()
        sign.jars.from(jar)
        sign.keystore.set(project.file("keystore").also { it.writeText("") })
        sign.alias.set("a")
        sign.storePassword.set(CredentialReference.Literal("s"))
        sign.outputDirectory.set(jars)
        shouldThrow<InvalidUserDataException> { sign.runActions() }
        jar.exists() shouldBe true

        val regexp = project.tasks.register("regexp", AntReplaceRegexp::class.java).get()
        regexp.sourceDirectory.set(source)
        regexp.outputDirectory.set(project.layout.projectDirectory)
        regexp.match.set("a")
        regexp.replace.set("b")
        shouldThrow<InvalidUserDataException> { regexp.runActions() }
        File(source, "a.xml").exists() shouldBe true
    }

    test("files with the same name fail instead of overwriting each other's outputs") {
        val project = project()
        val first = File(project.dir("one"), "x.txt").also { it.writeText("1") }
        val second = File(project.dir("two"), "x.txt").also { it.writeText("2") }
        val checksum = project.tasks.register("checksum", AntChecksum::class.java).get()
        checksum.files.from(first, second)
        checksum.outputDirectory.set(project.dir("sums"))
        shouldThrow<InvalidUserDataException> { checksum.runActions() }.message shouldContain "x.txt"

        val sign = project.tasks.register("sign", AntSignJar::class.java).get()
        sign.jars.from(File(project.dir("a"), "lib.jar"), File(project.dir("b"), "lib.jar"))
        sign.keystore.set(project.file("keystore").also { it.writeText("") })
        sign.alias.set("a")
        sign.storePassword.set(CredentialReference.Literal("s"))
        sign.outputDirectory.set(project.dir("signed"))
        shouldThrow<InvalidUserDataException> { sign.runActions() }.message shouldContain "lib.jar"
    }

    test("typed tasks default to class-loader isolation and get the plugin's classpath") {
        val project = project()
        val plain = project.tasks.register("plain", AntChecksum::class.java).get()
        plain.isolation.get() shouldBe AntIsolation.CLASSLOADER
        project.plugins.apply(AntBridgePlugin::class.java)
        val wired = project.tasks.register("wired", AntChecksum::class.java).get()
        wired.antClasspath.from.isEmpty() shouldBe false
    }
})
