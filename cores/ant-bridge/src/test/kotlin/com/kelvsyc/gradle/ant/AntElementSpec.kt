package com.kelvsyc.gradle.ant

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files

class AntElementSpec : FunSpec({
    val project = ProjectBuilder.builder().build()

    test("resolves files to absolute paths") {
        val file = File(Files.createTempDirectory("ant bridge").toFile(), "café file.txt")
        val element = AntElement("copy")
        element.attributes["file"] = file
        element.attributes["path"] = file.toPath()
        element.attributes["provided"] = project.providers.provider { project.layout.projectDirectory.file("x") }
        val node = element.resolve()
        node.attributes["file"] shouldBe file.absolutePath
        node.attributes["path"] shouldBe file.absolutePath
        node.attributes["provided"] shouldBe project.layout.projectDirectory.file("x").asFile.absolutePath
    }

    test("resolves file collections to a path string") {
        val a = project.file("a.txt")
        val b = project.file("b.txt")
        val element = AntElement("path")
        element.attributes["location"] = project.files(a, b)
        element.resolve().attributes["location"] shouldBe "${a.absolutePath}${File.pathSeparator}${b.absolutePath}"
    }

    test("resolves providers lazily") {
        var counter = 0
        val element = AntElement("echo")
        element.attributes["message"] = project.providers.provider { counter++; "x" }
        element.text = project.providers.provider { "body" }
        counter shouldBe 0
        val node = element.resolve()
        counter shouldBe 1
        node.attributes["message"] shouldBe "x"
        node.text shouldBe "body"
    }

    test("resolves scalars with toString") {
        val element = AntElement("x")
        element.attributes["n"] = 3
        element.attributes["b"] = true
        element.resolve().attributes shouldBe mapOf("n" to "3", "b" to "true")
    }

    test("absent provider fails naming element and attribute") {
        val element = AntElement("copy")
        element.attributes["todir"] = project.providers.provider<String> { null }
        shouldThrow<AntAttributeException> { element.resolve() }.message shouldBe "copy@todir has no value"
    }

    test("null attribute fails the same way") {
        val element = AntElement("copy")
        element.attributes["todir"] = null
        shouldThrow<AntAttributeException> { element.resolve() }.message shouldBe "copy@todir has no value"
    }

    test("provider of a file resolves recursively") {
        val file = File("some/file")
        val element = AntElement("x")
        element.attributes["f"] = project.providers.provider { file }
        element.resolve().attributes["f"] shouldBe file.absolutePath
    }

    test("absent text fails naming the element") {
        val element = AntElement("echo")
        element.text = project.providers.provider<String> { null }
        shouldThrow<AntAttributeException> { element.resolve() }.message shouldBe "echo text has no value"
    }

    test("children and namespace are preserved") {
        val parent = AntElement("target", "antlib:x")
        parent.attributes["b"] = "2"
        parent.attributes["a"] = "1"
        parent.children += AntElement("child").also { it.text = "t" }
        val node = parent.resolve()
        node.namespace shouldBe "antlib:x"
        node.attributes.keys.toList() shouldBe listOf("b", "a")
        node.children.single().name shouldBe "child"
        node.children.single().namespace shouldBe null
        node.children.single().text shouldBe "t"
    }

    test("AntNode is java-serializable") {
        val parent = AntElement("p", "ns")
        parent.attributes["k"] = "v"
        parent.children += AntElement("c")
        val node = parent.resolve()
        val bytes = ByteArrayOutputStream().also { ObjectOutputStream(it).use { out -> out.writeObject(node) } }
        val copy = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() } as AntNode
        copy.name shouldBe "p"
        copy.namespace shouldBe "ns"
        copy.attributes shouldBe mapOf("k" to "v")
        copy.children.single().name shouldBe "c"
    }

    test("toShimMap mirrors the node") {
        val parent = AntElement("p", "ns")
        parent.attributes["k"] = "v"
        parent.text = "t"
        parent.children += AntElement("c")
        parent.resolve().toShimMap() shouldBe mapOf(
            "name" to "p",
            "namespace" to "ns",
            "attributes" to mapOf("k" to "v"),
            "text" to "t",
            "children" to listOf(
                mapOf(
                    "name" to "c",
                    "namespace" to null,
                    "attributes" to emptyMap<String, String>(),
                    "text" to null,
                    "children" to emptyList<Any>()
                )
            )
        )
    }
})
