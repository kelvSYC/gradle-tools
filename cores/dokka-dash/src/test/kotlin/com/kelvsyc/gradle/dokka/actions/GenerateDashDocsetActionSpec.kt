package com.kelvsyc.gradle.dokka.actions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files
import java.sql.DriverManager

class GenerateDashDocsetActionSpec : FunSpec() {
    init {
        context("execute") {
            test("creates docset bundle structure with Info.plist and copies HTML") {
                val project = ProjectBuilder.builder().build()
                val dokkaDir = Files.createTempDirectory("dokka-input").toFile()
                val docsetDir = Files.createTempDirectory("docset-output").toFile()

                dokkaDir.resolve("index.html").writeText("<html><body>Hello</body></html>")
                dokkaDir.resolve("styles").mkdirs()
                dokkaDir.resolve("styles/main.css").writeText("body { }")

                val params = project.objects.newInstance<GenerateDashDocsetAction.Parameters>()
                params.dokkaOutputDirectory.set(dokkaDir)
                params.docsetName.set("TestLib")
                params.bundleIdentifier.set("com.example.testlib")
                params.docsetDirectory.set(docsetDir)

                val action = object : GenerateDashDocsetAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val plist = docsetDir.resolve("Contents/Info.plist")
                plist.exists().shouldBeTrue()
                val plistContent = plist.readText()
                plistContent shouldContain "com.example.testlib"
                plistContent shouldContain "TestLib"

                val copiedIndex = docsetDir.resolve("Contents/Resources/Documents/index.html")
                copiedIndex.exists().shouldBeTrue()
                copiedIndex.readText() shouldBe "<html><body>Hello</body></html>"

                val copiedCss = docsetDir.resolve("Contents/Resources/Documents/styles/main.css")
                copiedCss.exists().shouldBeTrue()

                dokkaDir.deleteRecursively()
                docsetDir.deleteRecursively()
            }

            test("creates SQLite search index from navigation.json") {
                val project = ProjectBuilder.builder().build()
                val dokkaDir = Files.createTempDirectory("dokka-input").toFile()
                val docsetDir = Files.createTempDirectory("docset-output").toFile()

                dokkaDir.resolve("index.html").writeText("<html/>")
                dokkaDir.resolve("navigation.json").writeText(
                    """
                    [
                        {
                            "label": "MyClass",
                            "location": "com/example/-my-class/index.html",
                            "icon": "class",
                            "children": [
                                {
                                    "label": "myFunction",
                                    "location": "com/example/-my-class/my-function.html",
                                    "icon": "function"
                                }
                            ]
                        },
                        {
                            "label": "MyInterface",
                            "location": "com/example/-my-interface/index.html",
                            "icon": "interface"
                        }
                    ]
                    """.trimIndent()
                )

                val params = project.objects.newInstance<GenerateDashDocsetAction.Parameters>()
                params.dokkaOutputDirectory.set(dokkaDir)
                params.docsetName.set("TestLib")
                params.bundleIdentifier.set("com.example.testlib")
                params.docsetDirectory.set(docsetDir)

                val action = object : GenerateDashDocsetAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val dbFile = docsetDir.resolve("Contents/Resources/docSet.dsidx")
                dbFile.exists().shouldBeTrue()

                Class.forName("org.sqlite.JDBC")
                val entries = mutableListOf<Triple<String, String, String>>()
                DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
                    conn.createStatement().use { stmt ->
                        val rs = stmt.executeQuery("SELECT name, type, path FROM searchIndex ORDER BY name")
                        while (rs.next()) {
                            entries.add(Triple(rs.getString("name"), rs.getString("type"), rs.getString("path")))
                        }
                    }
                }

                entries shouldContainExactlyInAnyOrder listOf(
                    Triple("MyClass", "Class", "com/example/-my-class/index.html"),
                    Triple("myFunction", "Function", "com/example/-my-class/my-function.html"),
                    Triple("MyInterface", "Interface", "com/example/-my-interface/index.html"),
                )

                dokkaDir.deleteRecursively()
                docsetDir.deleteRecursively()
            }

            test("creates empty index when navigation.json is absent") {
                val project = ProjectBuilder.builder().build()
                val dokkaDir = Files.createTempDirectory("dokka-input").toFile()
                val docsetDir = Files.createTempDirectory("docset-output").toFile()

                dokkaDir.resolve("index.html").writeText("<html/>")

                val params = project.objects.newInstance<GenerateDashDocsetAction.Parameters>()
                params.dokkaOutputDirectory.set(dokkaDir)
                params.docsetName.set("TestLib")
                params.bundleIdentifier.set("com.example.testlib")
                params.docsetDirectory.set(docsetDir)

                val action = object : GenerateDashDocsetAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val dbFile = docsetDir.resolve("Contents/Resources/docSet.dsidx")
                dbFile.exists().shouldBeTrue()

                Class.forName("org.sqlite.JDBC")
                DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
                    conn.createStatement().use { stmt ->
                        val rs = stmt.executeQuery("SELECT COUNT(*) FROM searchIndex")
                        rs.next()
                        rs.getInt(1) shouldBe 0
                    }
                }

                dokkaDir.deleteRecursively()
                docsetDir.deleteRecursively()
            }

            test("Info.plist uses custom indexPage when specified") {
                val project = ProjectBuilder.builder().build()
                val dokkaDir = Files.createTempDirectory("dokka-input").toFile()
                val docsetDir = Files.createTempDirectory("docset-output").toFile()

                dokkaDir.resolve("custom.html").writeText("<html/>")

                val params = project.objects.newInstance<GenerateDashDocsetAction.Parameters>()
                params.dokkaOutputDirectory.set(dokkaDir)
                params.docsetName.set("TestLib")
                params.bundleIdentifier.set("com.example.testlib")
                params.indexPage.set("custom.html")
                params.docsetDirectory.set(docsetDir)

                val action = object : GenerateDashDocsetAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val plistContent = docsetDir.resolve("Contents/Info.plist").readText()
                plistContent shouldContain "custom.html"

                dokkaDir.deleteRecursively()
                docsetDir.deleteRecursively()
            }

            test("icon types are mapped to correct Dash types") {
                val project = ProjectBuilder.builder().build()
                val dokkaDir = Files.createTempDirectory("dokka-input").toFile()
                val docsetDir = Files.createTempDirectory("docset-output").toFile()

                dokkaDir.resolve("index.html").writeText("<html/>")
                dokkaDir.resolve("navigation.json").writeText(
                    """
                    [
                        {"label": "MyEnum", "location": "enum.html", "icon": "enum_class"},
                        {"label": "MyAnnotation", "location": "anno.html", "icon": "annotation"},
                        {"label": "MyObject", "location": "obj.html", "icon": "object"},
                        {"label": "MyTypeAlias", "location": "alias.html", "icon": "type_alias"},
                        {"label": "myProp", "location": "prop.html", "icon": "property"},
                        {"label": "myPkg", "location": "pkg.html", "icon": "packages"},
                        {"label": "unknown", "location": "unk.html", "icon": "unknown_icon"}
                    ]
                    """.trimIndent()
                )

                val params = project.objects.newInstance<GenerateDashDocsetAction.Parameters>()
                params.dokkaOutputDirectory.set(dokkaDir)
                params.docsetName.set("TestLib")
                params.bundleIdentifier.set("com.example.testlib")
                params.docsetDirectory.set(docsetDir)

                val action = object : GenerateDashDocsetAction() {
                    override fun getParameters() = params
                }
                action.execute()

                Class.forName("org.sqlite.JDBC")
                val entries = mutableMapOf<String, String>()
                val dbFile = docsetDir.resolve("Contents/Resources/docSet.dsidx")
                DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
                    conn.createStatement().use { stmt ->
                        val rs = stmt.executeQuery("SELECT name, type FROM searchIndex")
                        while (rs.next()) {
                            entries[rs.getString("name")] = rs.getString("type")
                        }
                    }
                }

                entries["MyEnum"] shouldBe "Enum"
                entries["MyAnnotation"] shouldBe "Annotation"
                entries["MyObject"] shouldBe "Object"
                entries["MyTypeAlias"] shouldBe "Type"
                entries["myProp"] shouldBe "Property"
                entries["myPkg"] shouldBe "Package"
                entries.containsKey("unknown") shouldBe false

                dokkaDir.deleteRecursively()
                docsetDir.deleteRecursively()
            }
        }
    }
}
