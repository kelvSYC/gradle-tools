package com.kelvsyc.gradle.dokka.actions

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.io.File
import java.sql.DriverManager

/**
 * [WorkAction] that assembles a Dash docset from Dokka HTML output.
 *
 * Runs under process isolation so that the sqlite-jdbc native library is loaded in a dedicated
 * JVM rather than the Gradle daemon. Copies the HTML tree into the bundle, writes `Info.plist`,
 * and creates the SQLite search index (`docSet.dsidx`) from symbol entries parsed out of
 * Dokka's `navigation.json`.
 */
abstract class GenerateDashDocsetAction : WorkAction<GenerateDashDocsetAction.Parameters> {
    // PreparedStatement parameter indices are 1-based.
    private companion object {
        const val PARAM_NAME = 1
        const val PARAM_TYPE = 2
        const val PARAM_PATH = 3
    }

    interface Parameters : WorkParameters {
        val dokkaOutputDirectory: DirectoryProperty
        val docsetName: Property<String>
        val bundleIdentifier: Property<String>
        val indexPage: Property<String>
        /** The `.docset` bundle root; the action writes all output directly into this directory. */
        val docsetDirectory: DirectoryProperty
    }

    private data class IndexEntry(val name: String, val type: String, val path: String)

    override fun execute() {
        val dokkaDir = parameters.dokkaOutputDirectory.get().asFile
        val docsetName = parameters.docsetName.get()
        val bundleIdentifier = parameters.bundleIdentifier.get()

        // A Dash docset is an Apple bundle: <name>.docset/Contents/Resources/Documents/
        // The HTML tree lives under Documents/; the SQLite index and Info.plist sit in Resources/.
        val docsetDir = parameters.docsetDirectory.get().asFile
        val contentsDir = docsetDir.resolve("Contents")
        val resourcesDir = contentsDir.resolve("Resources")
        val documentsDir = resourcesDir.resolve("Documents")
        documentsDir.mkdirs()

        dokkaDir.copyRecursively(documentsDir, overwrite = true)
        writePlist(
            contentsDir.resolve("Info.plist"),
            docsetName,
            bundleIdentifier,
            parameters.indexPage.getOrElse("index.html"),
        )

        // navigation.json is Dokka's machine-readable navigation tree. It may be absent for
        // single-module outputs or older Dokka versions; an empty index is still a valid docset.
        val navigationFile = dokkaDir.resolve("navigation.json")
        val entries = if (navigationFile.exists()) parseNavigation(navigationFile) else emptyList()
        createIndex(resourcesDir.resolve("docSet.dsidx"), entries)
    }

    // Info.plist keys are defined by the Dash docset format specification.
    // DocSetPlatformFamily drives Dash's icon selection and must be lowercase.
    // isJavaScriptEnabled is required for Dokka's client-side search to work inside Dash.
    private fun writePlist(file: File, name: String, identifier: String, indexPage: String) {
        file.writeText(
            buildString {
                appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
                appendLine("""<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">""")
                appendLine("""<plist version="1.0">""")
                appendLine("<dict>")
                appendLine("    <key>CFBundleIdentifier</key>")
                appendLine("    <string>$identifier</string>")
                appendLine("    <key>CFBundleName</key>")
                appendLine("    <string>$name</string>")
                appendLine("    <key>DocSetPlatformFamily</key>")
                appendLine("    <string>${identifier.lowercase()}</string>")
                appendLine("    <key>isDashDocset</key>")
                appendLine("    <true/>")
                appendLine("    <key>dashIndexFilePath</key>")
                appendLine("    <string>$indexPage</string>")
                appendLine("    <key>isJavaScriptEnabled</key>")
                appendLine("    <true/>")
                appendLine("</dict>")
                append("</plist>")
            }
        )
    }

    private fun parseNavigation(file: File): List<IndexEntry> {
        val entries = mutableListOf<IndexEntry>()
        traverseNavigation(JsonParser.parseReader(file.reader()), entries)
        return entries
    }

    // navigation.json is a recursive tree: the root may be an array (multi-module) or an object
    // (single-module). Only leaf-like nodes with a known icon produce index entries; intermediate
    // nodes (e.g. packages acting purely as containers) are visited but not indexed if their icon
    // does not map to a Dash type.
    private fun traverseNavigation(element: JsonElement, entries: MutableList<IndexEntry>) {
        when {
            element.isJsonArray -> element.asJsonArray.forEach { traverseNavigation(it, entries) }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val location = obj.get("location")?.asString
                val label = obj.get("label")?.asString
                val icon = obj.get("icon")?.asString
                if (location != null && label != null && icon != null) {
                    toDashType(icon)?.let { dashType -> entries.add(IndexEntry(label, dashType, location)) }
                }
                obj.get("children")?.let { traverseNavigation(it, entries) }
            }
        }
    }

    // Icon names come from Dokka's NavigationNode.Icon sealed class (dokka-html module).
    // Unrecognised icons return null and are silently skipped so that new Dokka icon variants
    // do not break existing docsets.
    private fun toDashType(icon: String): String? = when (icon.lowercase()) {
        "class", "abstract_class", "exception" -> "Class"
        "interface", "abstract_interface" -> "Interface"
        "object", "companion_object" -> "Object"
        "enum_class" -> "Enum"
        "annotation" -> "Annotation"
        "type_alias" -> "Type"
        "function", "abstract_function", "extension_function" -> "Function"
        "property", "abstract_property", "extension_property" -> "Property"
        "packages" -> "Package"
        else -> null
    }

    private fun createIndex(file: File, entries: List<IndexEntry>) {
        // The JDBC driver is loaded via process isolation and will not have been registered by
        // the ServiceLoader in the worker JVM, so we force-load it here.
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS searchIndex(id INTEGER PRIMARY KEY, name TEXT, type TEXT, path TEXT)"
                )
                // The index name "anchor" and the unique constraint on (name, type, path) are
                // both required by the Dash docset format specification.
                stmt.executeUpdate(
                    "CREATE UNIQUE INDEX IF NOT EXISTS anchor ON searchIndex (name, type, path)"
                )
            }
            conn.prepareStatement("INSERT OR IGNORE INTO searchIndex(name, type, path) VALUES (?, ?, ?)").use { stmt ->
                for (entry in entries) {
                    stmt.setString(PARAM_NAME, entry.name)
                    stmt.setString(PARAM_TYPE, entry.type)
                    stmt.setString(PARAM_PATH, entry.path)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        }
    }
}
