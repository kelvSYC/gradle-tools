import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.add
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * Verifies that every class, method and field that a core's code and its runtime dependencies reference in Gradle
 * (`org.gradle.**`) or the Kotlin standard library (`kotlin.**`) exists in the oldest Gradle its tier supports.
 *
 * Each reference is resolved through the class hierarchy in [baseline], falling back to the JDK running the task.
 * The `kotlin-stdlib` and `kotlin-reflect` jars in [classesToCheck] are not scanned, since Gradle supplies its own;
 * references into them from everything else are checked against [baseline] instead.
 *
 * The task fails if any reference does not resolve, listing each as `source -> reference`, where the source is the
 * jar or class directory that makes it.
 */
@CacheableTask
abstract class VerifyGradleLinkage : DefaultTask() {
    /**
     * The classes to check: `main`'s output, plus its runtime classpath as resolved by a consumer running the tier's
     * oldest Gradle. Jars and class directories are both scanned.
     */
    @get:Classpath
    abstract val classesToCheck: ConfigurableFileCollection

    /**
     * What the tier's oldest Gradle provides: its public API and the libraries that API exposes, and `kotlin-stdlib`
     * and `kotlin-reflect` at its embedded Kotlin version.
     */
    @get:Classpath
    abstract val baseline: ConfigurableFileCollection

    /**
     * The report of missing references, one `source -> reference` per line, or a line saying there are none.
     */
    @get:OutputFile
    abstract val report: RegularFileProperty

    /**
     * Checks every reference in [classesToCheck] against [baseline] and writes [report].
     */
    @TaskAction
    fun verify() {
        val missing = ClassHierarchy(baseline.files).use { hierarchy ->
            classesToCheck.files.filterNot(::isSuppliedByGradle).flatMap { root ->
                referencesIn(root).filterNot(hierarchy::resolves).map { "${sourceName(root)} -> $it" }
            }.toSortedSet()
        }
        val file = report.get().asFile
        file.writeText(if (missing.isEmpty()) "No missing references.\n" else missing.joinToString("\n", postfix = "\n"))
        if (missing.isNotEmpty()) {
            throw GradleException(
                "${missing.size} references are missing from the oldest Gradle this project supports " +
                    "(see $file):\n" + missing.joinToString("\n")
            )
        }
    }

    private fun isSuppliedByGradle(file: File) =
        file.isFile && (file.name.startsWith("kotlin-stdlib") || file.name.startsWith("kotlin-reflect"))

    private fun sourceName(root: File) = if (root.isFile) root.name else root.path
}

/**
 * Registers the [VerifyGradleLinkage] task `verifyGradleLinkage`, which checks `main` and its runtime dependencies
 * against the oldest Gradle in [tier].
 *
 * The runtime dependencies are resolved as a consumer on that Gradle would resolve them: with `runtimeClasspath`'s
 * attributes, and `org.gradle.plugin.api-version` set to [GradleTier.minimum].
 *
 * The project's `check` task depends on it.
 */
fun Project.registerGradleLinkageCheck(tier: GradleTier) {
    val main = extensions.getByType(SourceSetContainer::class.java).named(SourceSet.MAIN_SOURCE_SET_NAME)
    val runtimeAttributes = configurations.named(main.get().runtimeClasspathConfigurationName).map { it.attributes }
    val compileAttributes = configurations.named(main.get().compileClasspathConfigurationName).map { it.attributes }

    val linkageClasspath = configurations.resolvable("gradleLinkageClasspath") {
        extendsFrom(
            configurations.getByName(main.get().implementationConfigurationName),
            configurations.getByName(main.get().runtimeOnlyConfigurationName),
        )
        attributes {
            copyFrom(runtimeAttributes.get())
            attribute(
                GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                objects.named(GradlePluginApiVersion::class.java, tier.minimum),
            )
        }
    }

    val baselineDependencies = configurations.dependencyScope("gradleLinkageBaselineDependencies")
    val baseline = configurations.resolvable("gradleLinkageBaseline") {
        extendsFrom(baselineDependencies.get())
        // The Gradle API only publishes an API variant.
        attributes { copyFrom(compileAttributes.get()) }
    }
    // The Gradle API's own dependencies (Groovy, SLF4J, Ant, ...) are part of the API Gradle exposes, and supertypes
    // of some of its types.
    dependencies.add(
        baselineDependencies.name,
        "org.gradle.experimental:gradle-public-api:${tier.publicApiVersion}",
    ) {
        gradlePublicApi()
    }
    listOf("kotlin-stdlib", "kotlin-reflect").forEach { module ->
        dependencies.add(baselineDependencies.name, "org.jetbrains.kotlin:$module") {
            version { strictly(tier.embeddedKotlin) }
            isTransitive = false
        }
    }

    val verify = tasks.register("verifyGradleLinkage", VerifyGradleLinkage::class.java) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks that main and its runtime dependencies link against Gradle ${tier.minimum}."
        classesToCheck.from(main.map { it.output }, linkageClasspath)
        this.baseline.from(baseline)
        report.set(layout.buildDirectory.file("reports/gradle-linkage/report.txt"))
    }
    tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { dependsOn(verify) }
}

private fun AttributeContainer.copyFrom(source: AttributeContainer) {
    source.keySet().forEach { key ->
        @Suppress("UNCHECKED_CAST")
        attribute(key as Attribute<Any>, checkNotNull(source.getAttribute(key)))
    }
}

/**
 * A reference to a class, or to a method or field of a class.
 *
 * @property owner The internal name of the class.
 * @property member The name and descriptor of a method, the name of a field, or `null` for the class itself.
 * @property isField Whether [member] is a field.
 */
private data class Reference(val owner: String, val member: String? = null, val isField: Boolean = false) {
    override fun toString() = if (member == null) owner else "$owner.$member"
}

private const val CLASS_SUFFIX = ".class"

private fun isClassEntry(name: String) = name.endsWith(CLASS_SUFFIX) && !name.startsWith("META-INF/")

/** Returns the references to Gradle and the Kotlin standard library in every class in a jar or class directory. */
private fun referencesIn(root: File): Set<Reference> {
    val references = HashSet<Reference>()
    when {
        root.isDirectory -> root.walk()
            .filter { it.isFile && isClassEntry(it.relativeTo(root).invariantSeparatorsPath) }
            .forEach { file -> file.inputStream().use { ReferenceCollector.collect(it, references) } }
        root.isFile && root.name.endsWith(".jar") -> ZipFile(root).use { zip ->
            zip.entries().asSequence().filter { isClassEntry(it.name) }.forEach { entry ->
                zip.getInputStream(entry).use { ReferenceCollector.collect(it, references) }
            }
        }
    }
    return references
}

/**
 * Collects references to the `org.gradle` and `kotlin` packages, and their subpackages, from the declarations and code
 * of a class.
 */
private class ReferenceCollector(private val references: MutableSet<Reference>) : ClassVisitor(Opcodes.ASM9) {
    companion object {
        fun collect(input: InputStream, references: MutableSet<Reference>) {
            ClassReader(input).accept(ReferenceCollector(references), ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }

        private fun inScope(owner: String) = owner.startsWith("org/gradle/") || owner.startsWith("kotlin/")

        /** Strips array dimensions and the `L...;` wrapper from an array type's internal name. */
        private fun elementName(name: String): String {
            val element = name.trimStart('[')
            return if (element.startsWith("L") && element.endsWith(";")) element.substring(1, element.length - 1)
            else element
        }
    }

    private fun classRef(name: String?) {
        val owner = name?.let(::elementName)
        if (owner != null && inScope(owner)) references += Reference(owner)
    }

    private fun memberRef(owner: String, member: String, isField: Boolean, descriptor: String) {
        val element = elementName(owner)
        if (inScope(element)) references += Reference(element, member, isField)
        descriptorRefs(descriptor)
    }

    private fun typeRef(type: Type) {
        val element = if (type.sort == Type.ARRAY) type.elementType else type
        if (element.sort == Type.OBJECT) classRef(element.internalName)
    }

    private fun descriptorRefs(descriptor: String) {
        val type = Type.getType(descriptor)
        if (type.sort == Type.METHOD) {
            type.argumentTypes.forEach(::typeRef)
            typeRef(type.returnType)
        } else {
            typeRef(type)
        }
    }

    private fun constantRef(value: Any?) {
        when (value) {
            is Type -> if (value.sort == Type.METHOD) descriptorRefs(value.descriptor) else typeRef(value)
            is Handle -> {
                val isField = value.tag <= Opcodes.H_PUTSTATIC
                memberRef(value.owner, if (isField) value.name else value.name + value.desc, isField, value.desc)
            }
        }
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) {
        classRef(superName)
        interfaces?.forEach(::classRef)
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor? {
        descriptorRefs(descriptor)
        return null
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        descriptorRefs(descriptor)
        exceptions?.forEach(::classRef)
        return object : MethodVisitor(Opcodes.ASM9) {
            override fun visitTypeInsn(opcode: Int, type: String) = classRef(type)

            override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) =
                memberRef(owner, name, true, descriptor)

            override fun visitMethodInsn(
                opcode: Int,
                owner: String,
                name: String,
                descriptor: String,
                isInterface: Boolean,
            ) = memberRef(owner, name + descriptor, false, descriptor)

            override fun visitLdcInsn(value: Any?) = constantRef(value)

            override fun visitInvokeDynamicInsn(
                name: String,
                descriptor: String,
                bootstrapMethodHandle: Handle,
                vararg bootstrapMethodArguments: Any?,
            ) {
                descriptorRefs(descriptor)
                constantRef(bootstrapMethodHandle)
                bootstrapMethodArguments.forEach(::constantRef)
            }

            override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) = descriptorRefs(descriptor)

            override fun visitTryCatchBlock(
                start: Label,
                end: Label,
                handler: Label,
                type: String?,
            ) = classRef(type)
        }
    }
}

/**
 * The declared supertypes, methods and fields of a class.
 *
 * @property superName The internal name of the superclass, or `null` for `java/lang/Object` and modules.
 * @property interfaces The internal names of the interfaces the class implements.
 * @property methods The name and descriptor of every method the class declares.
 * @property fields The name of every field the class declares.
 */
private class ClassHeader(
    val superName: String?,
    val interfaces: List<String>,
    val methods: Set<String>,
    val fields: Set<String>,
) {
    companion object {
        fun read(input: InputStream): ClassHeader {
            var superName: String? = null
            var interfaces = emptyList<String>()
            val methods = HashSet<String>()
            val fields = HashSet<String>()
            val visitor = object : ClassVisitor(Opcodes.ASM9) {
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName0: String?,
                    interfaces0: Array<out String>?,
                ) {
                    superName = superName0
                    interfaces = interfaces0?.toList().orEmpty()
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?,
                ): MethodVisitor? {
                    methods += name + descriptor
                    return null
                }

                override fun visitField(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    value: Any?,
                ): FieldVisitor? {
                    fields += name
                    return null
                }
            }
            ClassReader(input).accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            return ClassHeader(superName, interfaces, methods, fields)
        }
    }
}

/**
 * Resolves references through the class hierarchy of a set of jars, then of the JDK running the task.
 */
private class ClassHierarchy(jars: Set<File>) : AutoCloseable {
    private val zips = jars.filter { it.isFile }.map(::ZipFile)
    private val headers = HashMap<String, ClassHeader?>()
    private val jdk = ClassLoader.getPlatformClassLoader()

    private fun header(name: String): ClassHeader? =
        if (name in headers) headers[name] else load(name).also { headers[name] = it }

    private fun load(name: String): ClassHeader? {
        val path = name + CLASS_SUFFIX
        val input = zips.firstNotNullOfOrNull { zip -> zip.getEntry(path)?.let(zip::getInputStream) }
            ?: jdk.getResourceAsStream(path)
        return input?.use(ClassHeader::read)
    }

    /** Returns whether [reference] exists, declared by its owner or inherited from a supertype. */
    fun resolves(reference: Reference): Boolean =
        if (reference.member == null) header(reference.owner) != null
        else declares(reference.owner, reference, HashSet())

    private fun declares(owner: String?, reference: Reference, seen: MutableSet<String>): Boolean {
        val header = owner?.takeIf(seen::add)?.let(::header)
        return header != null && (
            reference.member in (if (reference.isField) header.fields else header.methods) ||
                declares(header.superName, reference, seen) ||
                header.interfaces.any { declares(it, reference, seen) }
            )
    }

    override fun close() = zips.forEach(ZipFile::close)
}
