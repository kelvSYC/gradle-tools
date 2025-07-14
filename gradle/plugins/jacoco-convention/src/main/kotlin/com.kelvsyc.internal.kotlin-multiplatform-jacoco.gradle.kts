import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.jacoco
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import java.util.Optional

plugins {
    jacoco
    id("com.kelvsyc.internal.kotlin-multiplatform-jvm-base")
}

// Because Kotlin Multiplatform plugin doesn't integrate with the JaCoCo plugin, we have to
// write our own integration.
// Defaults from jacoco plugin
val jacocoJvmTestReport by tasks.registering(JacocoReport::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Generates code coverage report for the 'jvmTest' task."

    dependsOn(tasks.named("jvmTest"))

    // This is how they do it in the jacoco plugin for Java sources, so we have to realize jvmTest here
    executionData(tasks.jvmTest.get())

    sourceDirectories.from(kotlin.sourceSets.commonMain.map { it.kotlin.sourceDirectories })
    sourceDirectories.from(kotlin.sourceSets.jvmMain.map { it.kotlin.sourceDirectories })
    classDirectories.from(kotlin.jvm().compilations.named("main").map { it.output.classesDirs })

    reports {
        html.required.set(true)
    }
}

tasks.jvmTest {
    finalizedBy(jacocoJvmTestReport)
}

// The two configurations below are needed to integrate with the JaCoCo Report Aggregation Plugin
@Suppress("UnstableApiUsage")
val coverageDataElements = configurations.consumable("coverageDataElementsForJvmTest") {
    // This configuration mimics that defined by the "jacoco" plugin
    description = "Binary results containing JaCoCo test coverage for the 'jvmTest' suites."

    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
    attributes.attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.JACOCO_RESULTS))
    // FIXME how do we accommodate multiple JVM targets and their respective test suites?
    attributes.attribute(TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE, objects.named("test"))

    val resultsFile = tasks.jvmTest.map {
            Optional.ofNullable(it.the<JacocoTaskExtension>().destinationFile)
        }
        .filter(Optional<File>::isPresent)
        .map(Optional<File>::get)
    outgoing.artifact(resultsFile) {
        type = ArtifactTypeDefinition.BINARY_DATA_TYPE
    }
}

@Suppress("UnstableApiUsage")
val jvmMainSourceElements = configurations.consumable("jvmMainSourceElements") {
    description = "List of source directories contained in the jvm target."

    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
    attributes.attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.MAIN_SOURCES))
    attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))

    outgoing.artifacts(kotlin.sourceSets.commonMain.map { it.kotlin.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
    outgoing.artifacts(kotlin.sourceSets.commonMain.map { it.resources.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
    outgoing.artifacts(kotlin.sourceSets.jvmMain.map { it.kotlin.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
    outgoing.artifacts(kotlin.sourceSets.jvmMain.map { it.resources.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
}
