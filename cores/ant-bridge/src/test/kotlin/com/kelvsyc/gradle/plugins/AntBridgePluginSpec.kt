package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.ant.AntBridgeExtension
import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder

class AntBridgePluginSpec : FunSpec({
    fun project(): Project = ProjectBuilder.builder().build().also { it.plugins.apply(AntBridgePlugin::class.java) }

    // withDependencies actions run lazily, when a configuration's dependencies are first queried through incoming.
    fun Project.realised(): Set<org.gradle.api.artifacts.Dependency> =
        configurations.getByName("antBridgeClasspath").incoming.dependencies.toSet()

    test("applying the plugin creates antBridge and antBridgeClasspath") {
        val project = project()
        project.configurations.getByName("antBridge").isCanBeResolved shouldBe false
        project.configurations.getByName("antBridgeClasspath").isCanBeResolved shouldBe true
    }

    test("antBridge carries Ant at the default version as a preference") {
        val deps = project().realised()
            .filterIsInstance<ExternalModuleDependency>().associateBy { "${it.group}:${it.name}" }
        deps.keys shouldContain "org.apache.ant:ant"
        deps.keys shouldContain "org.apache.ant:ant-junit"
        deps.values.forEach { it.versionConstraint.preferredVersion shouldBe AntBridgePlugin.DEFAULT_ANT_VERSION }
    }

    test("adding an antlib keeps Ant") {
        val project = project()
        project.dependencies.add("antBridge", "ant-contrib:ant-contrib:1.0b3")
        val names = project.realised().map { "${it.group}:${it.name}" }
        names shouldContain "org.apache.ant:ant"
        names shouldContain "ant-contrib:ant-contrib"
    }

    test("default version matches the catalog") {
        AntBridgePlugin.DEFAULT_ANT_VERSION shouldBe System.getProperty("antBridge.test.catalogAntVersion")
    }

    test("extension exposes the classpath") {
        val project = project()
        TestAnt.useIn(project)
        project.extensions.getByType<AntBridgeExtension>().classpath.files.map { it.name } shouldContain
            "ant-${AntBridgePlugin.DEFAULT_ANT_VERSION}.jar"
    }

    test("the plugin jar resources contain the shim") {
        AntBridgePlugin::class.java
            .getResource("/ant-bridge-shim/com/kelvsyc/gradle/ant/shim/ShimSupport.class.bin") shouldNotBe null
    }
})

