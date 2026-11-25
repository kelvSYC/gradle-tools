package com.kelvsyc.gradle.ant

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.Action
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class AntScopeSpec : FunSpec({
    val project = ProjectBuilder.builder().build()
    val src = File("/src")
    val dir = File("/dest")

    fun tree(body: Action<in AntScope>) = AntTrees.build(body).map { it.resolve() }

    test("generic task with nested elements") {
        val nodes = tree(
            Action {
                task("copy", "todir" to dir) {
                    element("fileset", "dir" to src) {
                        element("include", "name" to "**/*.xml")
                    }
                }
            }
        )
        nodes.map { it.toShimMap() } shouldBe listOf(
            AntNode(
                "copy", null, mapOf("todir" to dir.absolutePath), null,
                listOf(
                    AntNode(
                        "fileset", null, mapOf("dir" to src.absolutePath), null,
                        listOf(AntNode("include", null, mapOf("name" to "**/*.xml"), null, emptyList()))
                    )
                )
            ).toShimMap()
        )
    }

    test("fileset helper expands includes and excludes") {
        val nodes = tree(
            Action {
                task("delete") {
                    fileset(src, listOf("a", "b"), listOf("c")) { element("depth") }
                }
            }
        )
        val fileset = nodes.single().children.single()
        fileset.attributes shouldBe mapOf("dir" to src.absolutePath)
        fileset.children.map { it.name to it.attributes["name"] } shouldBe
            listOf("include" to "a", "include" to "b", "exclude" to "c", "depth" to null)
    }

    test("filterset helper emits one filter per token in order") {
        val nodes = tree(
            Action {
                task("copy") { filterset(tokens = linkedMapOf("z" to 1, "a" to "two")) }
            }
        )
        val filterset = nodes.single().children.single()
        filterset.attributes shouldBe mapOf("begintoken" to "@", "endtoken" to "@")
        filterset.children.map { it.attributes } shouldBe listOf(
            mapOf("token" to "z", "value" to "1"),
            mapOf("token" to "a", "value" to "two")
        )
    }

    test("namespace is carried") {
        val nodes = tree(
            Action {
                task("foo", namespace = "antlib:x") { element("bar", namespace = "antlib:y") }
            }
        )
        nodes.single().namespace shouldBe "antlib:x"
        nodes.single().children.single().namespace shouldBe "antlib:y"
    }

    test("path keeps the file collection lazy until resolve") {
        val files = project.files()
        val elements = AntTrees.build(Action { task("x") { path(files) } })
        val a = project.file("a.txt")
        files.from(a)
        elements.single().children.single().resolve().attributes["path"] shouldBe a.absolutePath
    }

    test("mapper omits absent attributes, filterchain and text nest") {
        val nodes = tree(
            Action {
                task("copy") {
                    mapper("glob", from = "*.a")
                    filterchain { element("tabstospaces"); text("t") }
                }
            }
        )
        nodes.single().children[0].attributes shouldBe mapOf("type" to "glob", "from" to "*.a")
        nodes.single().children[1].name shouldBe "filterchain"
        nodes.single().children[1].text shouldBe "t"
    }
})

