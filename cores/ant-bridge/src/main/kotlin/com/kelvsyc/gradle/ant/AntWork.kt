package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.PropertiesFiles
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.util.HexFormat
import javax.inject.Inject

/**
 * Runs an Ant tree, built at configuration time with [ant] (Kotlin DSL) or [record] (legacy `ant.xxx(...)` calls).
 *
 * Declare inputs and outputs with the normal `inputs`/`outputs` API; without declared outputs the task is never
 * up to date. The Ant tree itself is an input through [treeFingerprint], so editing it reruns the task. With the `com.kelvsyc.gradle.ant-bridge` plugin, [antClasspath] defaults to the `antBridgeClasspath`
 * configuration and [isolation] to [AntIsolation.CLASSLOADER].
 *
 * [exportProperties] writes Ant properties the tree sets to [exportedPropertiesFile], which other tasks read through
 * [exportedProperties]. Exporting alone never makes the task up to date: the task is up to date only when it has
 * outputs besides the export file and nothing changed. An exported value usually depends on something the task does
 * not declare (a `git` command, the clock), and would otherwise go stale.
 *
 * Configure this task; it is not designed to be extended. To define a reusable Ant task type, extend
 * [com.kelvsyc.gradle.ant.tasks.AbstractAntTask].
 */
@DisableCachingByDefault(because = "Arbitrary Ant work; declare outputs to get up-to-date checks")
abstract class AntWork @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    providers: ProviderFactory
) : DefaultTask() {
    /** The Ant tasks to run, in order; values stay unresolved until execution. */
    @get:Internal
    abstract val elements: ListProperty<AntElement>

    /**
     * The SHA-256 (lowercase hex) of the resolved [elements]: each element's name, namespace, attributes in order,
     * text and children. Resolved when Gradle snapshots the task's inputs, so a change to the Ant tree reruns the
     * task; only the hash is recorded, which keeps resolved values such as secrets out of the execution history.
     */
    @get:Input
    val treeFingerprint: Provider<String>
        get() = elements.map { fingerprint(it.map(AntElement::resolve)) }

    /** The Ant classpath: Ant, its antlibs and anything they need. */
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection

    /** How Ant is isolated from the build. */
    @get:Internal
    abstract val isolation: Property<AntIsolation>

    /**
     * The names of the Ant properties to export to [exportedPropertiesFile]. Read-only: add names with
     * [exportProperties], which also registers the export file as an output.
     */
    @get:Input
    val exportedPropertyNames: Provider<Set<String>>
        get() = exportedNames

    /**
     * The file the exported Ant properties are written to, in Java properties format (which Ant's `property file=`
     * reads). Defaults to `build/ant-bridge/<task name>/exported.properties` when any property is exported, and is
     * absent otherwise. [exportProperties] registers it as an output of the task; it is not annotated as one, because
     * Gradle counts any annotated output property, even an absent one, so a task that exports nothing would no longer
     * report having no outputs.
     */
    @get:Internal
    abstract val exportedPropertiesFile: RegularFileProperty

    /**
     * The exported Ant properties, read from [exportedPropertiesFile]; names the tree did not set are absent. Carries
     * the dependency on this task, so a task using it as an input runs after this one.
     */
    @get:Internal
    val exportedProperties: Provider<Map<String, String>>
        get() = exportedPropertiesFile.map { PropertiesFiles.read(it.asFile) }

    private val operations = objects.newInstance(AntOperations::class.java)

    private val exportedNames: SetProperty<String> = objects.setProperty(String::class.java).convention(emptySet())

    private var exportRegistered = false

    init {
        isolation.convention(AntIsolation.CLASSLOADER)
        exportedPropertiesFile.convention(
            exportedNames.flatMap { names ->
                if (names.isEmpty()) {
                    providers.provider<RegularFile> { null }
                } else {
                    layout.buildDirectory.file("ant-bridge/$name/exported.properties")
                }
            }
        )
    }

    /**
     * Exports the Ant properties named [names] after the tree runs: adds them to [exportedPropertyNames]. On the first
     * call with names it registers [exportedPropertiesFile] as an output (so [exportedProperties] carries the task
     * dependency) and the rule that the export file alone never makes the task up to date. Without names it does
     * nothing.
     */
    fun exportProperties(vararg names: String) {
        if (names.isEmpty()) return
        exportedNames.addAll(*names)
        if (!exportRegistered) {
            exportRegistered = true
            // Registered here, not in init: any output or up-to-date spec makes `outputs.hasOutput` true.
            outputs.file(exportedPropertiesFile).withPropertyName("exportedPropertiesFile").optional()
            outputs.upToDateWhen { task ->
                (task.outputs.files.files - exportedPropertiesFile.orNull?.asFile).isNotEmpty()
            }
        }
    }

    /**
     * Appends the Ant tasks of [body] (Kotlin DSL) to [elements].
     */
    fun ant(body: Action<in AntScope>) {
        elements.addAll(AntTrees.build(body))
    }

    /**
     * Appends the legacy `ant.xxx(...)` calls recorded by [body] to [elements].
     */
    fun record(body: Action<in AntRecorder>) {
        elements.addAll(AntTrees.record(body))
    }

    /**
     * Runs [elements] with [antClasspath], isolated as [isolation] says, and writes the properties named in
     * [exportedPropertyNames] to [exportedPropertiesFile].
     */
    @TaskAction
    fun run() {
        operations.run(
            antClasspath,
            isolation.get(),
            elements.get(),
            exportedNames.get(),
            // Only a registered export writes the file: without names it would be an undeclared output.
            if (exportRegistered) exportedPropertiesFile.orNull?.asFile else null
        )
    }

    private companion object {
        fun fingerprint(nodes: List<AntNode>): String {
            val bytes = ByteArrayOutputStream()
            DataOutputStream(bytes).use { out -> writeNodes(out, nodes) }
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()))
        }

        private fun writeNodes(out: DataOutputStream, nodes: List<AntNode>) {
            out.writeInt(nodes.size)
            nodes.forEach { node ->
                writeString(out, node.name)
                writeString(out, node.namespace)
                out.writeInt(node.attributes.size)
                node.attributes.forEach { (key, value) ->
                    writeString(out, key)
                    writeString(out, value)
                }
                writeString(out, node.text)
                writeNodes(out, node.children)
            }
        }

        /** Writes [value] length-prefixed, so that no two different trees render the same bytes. */
        private fun writeString(out: DataOutputStream, value: String?) {
            out.writeBoolean(value != null)
            if (value != null) {
                val encoded = value.toByteArray(Charsets.UTF_8)
                out.writeInt(encoded.size)
                out.write(encoded)
            }
        }
    }
}
