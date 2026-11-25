package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Ant's `signjar`: signs each of [jars] into [outputDirectory]. The passwords are credential references, resolved
 * at execution and never part of the task's inputs.
 */
@DisableCachingByDefault(because = "Signatures may carry timestamps and need secrets")
abstract class AntSignJar @Inject constructor(objects: ObjectFactory) : AbstractAntTask(objects) {
    /** The jars to sign. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val jars: ConfigurableFileCollection

    /** The keystore holding the signing key. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val keystore: RegularFileProperty

    /** The alias of the signing key. */
    @get:Input
    abstract val alias: Property<String>

    /** The keystore password; resolved at execution, not an input. */
    @get:Internal
    abstract val storePassword: Property<CredentialReference>

    /** The key password, if it differs from the keystore password; resolved at execution, not an input. */
    @get:Internal
    @get:Optional
    abstract val keyPassword: Property<CredentialReference>

    /** The keystore type (Ant's `storetype`). */
    @get:Input
    @get:Optional
    abstract val storeType: Property<String>

    /** The signature algorithm (Ant's `sigalg`). */
    @get:Input
    @get:Optional
    abstract val sigAlg: Property<String>

    /** The digest algorithm (Ant's `digestalg`). */
    @get:Input
    @get:Optional
    abstract val digestAlg: Property<String>

    /** The URL of the timestamping authority (Ant's `tsaurl`). */
    @get:Input
    @get:Optional
    abstract val tsaUrl: Property<String>

    /** The directory receiving the signed jars (Ant's `destDir`). */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    override fun elements(): List<AntElement> {
        val storePass = storePassword.get().resolve()
        val keyPass = keyPassword.orNull?.resolve()
        return jars.files.map { jar ->
            element(
                "signjar",
                "jar" to jar,
                "destDir" to outputDirectory,
                "keystore" to keystore,
                "alias" to alias,
                "storepass" to storePass,
                "keypass" to keyPass,
                "storetype" to storeType.orNull,
                "sigalg" to sigAlg.orNull,
                "digestalg" to digestAlg.orNull,
                "tsaurl" to tsaUrl.orNull
            )
        }
    }
}
