package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntScope
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.testfixtures.ProjectBuilder

/** The example of the README's "Defining a typed task" section, kept here so that it compiles and stays correct. */
@CacheableTask
abstract class Concat : AbstractAntTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val includes: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val header: Property<String>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    override fun AntScope.elements() {
        task("concat", "destfile" to destination, "fixlastline" to true) {
            header.orNull?.let { element("header", "trimleading" to true) { text(it) } }
            fileset(sourceDirectory, includes = includes.get())
            filterchain { element("striplinecomments") { element("comment", "value" to "#") } }
        }
    }
}

class DefiningATypedTaskSpec : FunSpec({
    test("the README example builds a nested tree and leaves out an absent optional element") {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.register("concat", Concat::class.java).get()
        task.sourceDirectory.set(project.file("src"))
        task.includes.set(listOf("**/*.txt"))
        task.destination.set(project.file("out/all.txt"))

        val concat = task.buildElements().single().resolve()
        concat.attributes shouldBe mapOf(
            "destfile" to project.file("out/all.txt").absolutePath,
            "fixlastline" to "true"
        )
        concat.children.map { it.name } shouldBe listOf("fileset", "filterchain")
        concat.children[0].children.single().attributes shouldBe mapOf("name" to "**/*.txt")

        task.header.set("# all")
        task.buildElements().single().resolve().children.first().text shouldBe "# all"
    }
})
