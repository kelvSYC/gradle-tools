package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntRuntimeClassLoader
import com.kelvsyc.gradle.ant.internal.AntShim
import com.kelvsyc.gradle.plugins.AntBridgePlugin
import com.kelvsyc.gradle.providers.getting
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class AntPropertiesLoadingSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().withProjectDir(tempdir()).build()
        .also { it.plugins.apply(AntBridgePlugin::class.java) }

    fun Project.load(configure: AntPropertiesSpec.() -> Unit): Provider<Map<String, String>> {
        val spec = objects.newInstance(AntPropertiesSpec::class.java).apply(configure)
        return providers.of(AntProperties::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.setFrom(TestAnt.classpath)
            }
        }
    }

    fun Project.write(name: String, content: String): File =
        File(projectDir, name).also { it.writeText(content) }

    test("expands references within and across files") {
        val project = project()
        project.write("a.properties", "base=/opt\ndir=${'$'}{base}/app\n")
        project.write("b.properties", "lib=${'$'}{dir}/lib\n")
        val result = project.load { file("a.properties"); file("b.properties") }.get()
        result["dir"] shouldBe "/opt/app"
        result["lib"] shouldBe "/opt/app/lib"
    }

    test("prefix and prefixValues") {
        val project = project()
        project.write("a.properties", "base=/opt\ndir=${'$'}{base}/app\n")
        val result = project.load {
            file("a.properties", Action { prefix.set("cfg"); prefixValues.set(true) })
        }.get()
        result.keys shouldBe setOf("cfg.base", "cfg.dir")
        result["cfg.dir"] shouldBe "/opt/app"
    }

    test("seeds override file values") {
        val project = project()
        project.write("a.properties", "base=/opt\ndir=${'$'}{base}/app\n")
        val result = project.load { seed("base", "/srv"); file("a.properties") }.get()
        result["base"] shouldBe "/srv"
        result["dir"] shouldBe "/srv/app"
    }

    test("earlier source wins for a duplicate key") {
        val project = project()
        project.write("a.properties", "k=first\n")
        project.write("b.properties", "k=second\n")
        val result = project.load { file("a.properties"); file("b.properties") }.get()
        result["k"] shouldBe "first"
        result.keys.toList() shouldContainExactly listOf("k")
    }

    test("missing file contributes nothing and logs at info") {
        val project = project()
        project.load { file("missing.properties") }.get().shouldBeEmpty()

        val missing = File(project.projectDir, "missing.properties")
        val logger = mockk<Logger>(relaxed = true)
        val request = AntShim.PropertyRequest(
            listOf(AntPropertySource.File(missing, null, false)),
            emptyMap(),
            project.projectDir
        )
        val result = AntRuntimeClassLoader(TestAnt.classpath).use { AntShim(it).loadProperties(request, logger) }
        result.shouldBeEmpty()
        verify { logger.info("Property file ${missing.absolutePath} not found; skipped") }
    }

    test("system properties and Ant built-ins are excluded") {
        val project = project()
        project.write("a.properties", "dist=${'$'}{basedir}/dist\nv=${'$'}{ant.version}\n")
        val result = project.load { file("a.properties") }.get()
        result.keys shouldContainExactlyInAnyOrder listOf("dist", "v")
        result["v"] shouldStartWith "Apache Ant(TM) version 1.10.18"
    }

    test("basedir is the project directory") {
        val project = project()
        project.write("a.properties", "dist=${'$'}{basedir}/dist\n")
        val viaSpec = project.load { file("a.properties") }.get()
        viaSpec["dist"] shouldBe "${project.projectDir.absolutePath}/dist"
        TestAnt.useIn(project)
        val viaExtension = project.extensions.getByType<AntBridgeExtension>()
            .properties { file("a.properties") }.get()
        viaExtension["dist"] shouldBe "${project.projectDir.absolutePath}/dist"
        viaExtension.keys shouldNotContain "basedir"
    }

    test("resource and url sources take prefix options") {
        val project = project()
        val jar = File(project.projectDir, "res.jar")
        JarOutputStream(jar.outputStream()).use {
            it.putNextEntry(JarEntry("res/r.properties"))
            it.write("base=/r\ndir=${'$'}{base}/app\n".toByteArray())
            it.closeEntry()
        }
        val urlFile = project.write("u.properties", "base=/u\ndir=${'$'}{base}/app\n")
        val result = project.load {
            resource("res/r.properties", project.files(jar), Action { prefix.set("res"); prefixValues.set(true) })
            url(urlFile.toURI().toString(), Action { prefix.set("url"); prefixValues.set(true) })
        }.get()
        result["res.dir"] shouldBe "/r/app"
        result["url.dir"] shouldBe "/u/app"
        result.keys shouldContainExactlyInAnyOrder listOf("res.base", "res.dir", "url.base", "url.dir")
    }

    test("key already set as a system property reports Ant's effective value") {
        val project = project()
        project.write("a.properties", "user.home=x\n")
        project.load { file("a.properties") }.get()["user.home"] shouldBe System.getProperty("user.home")
    }

    test("environment source uses the prefix") {
        val result = project().load { environment("env") }.get()
        result["env.PATH"] shouldBe System.getenv("PATH")
    }

    test("resource and url sources") {
        val project = project()
        val jar = File(project.projectDir, "res.jar")
        JarOutputStream(jar.outputStream()).use {
            it.putNextEntry(JarEntry("res/r.properties"))
            it.write("r=from-resource\n".toByteArray())
            it.closeEntry()
        }
        val urlFile = project.write("u.properties", "u=from-url\n")
        val result = project.load {
            resource("res/r.properties", project.files(jar))
            url(urlFile.toURI().toString())
        }.get()
        result["r"] shouldBe "from-resource"
        result["u"] shouldBe "from-url"
    }

    test("ISO-8859-1 and unicode escapes decode as Ant does") {
        val project = project()
        val bytes = "a=caf\\u00e9\nb=caf".toByteArray(Charsets.ISO_8859_1) + byteArrayOf(0xE9.toByte(), '\n'.code.toByte())
        File(project.projectDir, "a.properties").writeBytes(bytes)
        val result = project.load { file("a.properties") }.get()
        result["a"] shouldBe "café"
        result["b"] shouldBe "café"
    }

    test("getting is absent for a missing key") {
        val project = project()
        project.write("a.properties", "k=v\n")
        val provider = project.load { file("a.properties") }
        provider.getting("k").get() shouldBe "v"
        provider.getting("nope").isPresent shouldBe false
    }

    test("the antBridge extension loads properties with the plugin's classpath") {
        val project = project()
        TestAnt.useIn(project)
        project.write("a.properties", "base=/opt\ndir=${'$'}{base}/app\n")
        val result = project.extensions.getByType<AntBridgeExtension>()
            .properties { file("a.properties") }.get()
        result["dir"] shouldBe "/opt/app"
    }
})
