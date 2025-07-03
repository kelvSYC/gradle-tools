import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.jacoco
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import java.util.Optional

plugins {
    jacoco
    kotlin("multiplatform")
}

kotlin {
    // FIXME find a way to conditionally apply jacoco configuration on presence of a JVM target
    jvm()
}

// Because Kotlin Multiplatform plugin doesn't integrate with the JaCoCo plugin, we have to
// write our own integration.
// Defaults from jacoco plugin
val jacocoJvmTestReport by tasks.registering(JacocoReport::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Generates code coverage report for the 'jvmTest' task."

    dependsOn(tasks.named("jvmTest"))

    // Wish we could use mapKt() in gradle-extensions, but alas...
    executionData.from(
        tasks.named("jvmTest").map {
            Optional.ofNullable(it.the<JacocoTaskExtension>().destinationFile)
        }.
        filter(Optional<File>::isPresent).
        map(Optional<File>::get)
    )

//    executionData.from(layout.buildDirectory.file("jacoco/jvmTest.exec"))
    sourceDirectories.from(kotlin.sourceSets.commonMain.map { it.kotlin })
    sourceDirectories.from(kotlin.sourceSets.named("jvmMain").map { it.kotlin })
    classDirectories.from(layout.buildDirectory.dir("classes/kotlin/jvm"))

    reports {
        html.required.set(true)
    }
}

tasks.named("jvmTest") {
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

    val resultsFile = tasks.named("jvmTest")
        .map {
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
    outgoing.artifacts(kotlin.sourceSets.named("jvmMain").map { it.kotlin.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
    outgoing.artifacts(kotlin.sourceSets.named("jvmMain").map { it.resources.srcDirs }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
}
