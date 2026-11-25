package com.kelvsyc.gradle.ant

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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
 * Configure this task; it is not designed to be extended. To define a reusable Ant task type, extend
 * [com.kelvsyc.gradle.ant.tasks.AbstractAntTask].
 */
@DisableCachingByDefault(because = "Arbitrary Ant work; declare outputs to get up-to-date checks")
abstract class AntWork @Inject constructor(objects: ObjectFactory) : DefaultTask() {
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

    private val operations = objects.newInstance(AntOperations::class.java)

    init {
        isolation.convention(AntIsolation.CLASSLOADER)
    }

    /**
     * Appends the Ant tasks of [body] (Kotlin DSL) to [elements].
     */
    fun ant(body: Action<in AntScope>) {
        elements.addAll(buildTree(body))
    }

    /**
     * Appends the legacy `ant.xxx(...)` calls recorded by [body] to [elements].
     */
    fun record(body: Action<in AntRecorder>) {
        elements.addAll(AntRecorder.record { body.execute(it) })
    }

    /**
     * Runs [elements] with [antClasspath], isolated as [isolation] says.
     */
    @TaskAction
    fun run() {
        operations.run(antClasspath, isolation.get(), elements.get())
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
