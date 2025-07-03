@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import kotlin.jvm.optionals.getOrNull

plugins {
    kotlin("multiplatform")
    id("com.autonomousapps.dependency-analysis")
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.reproducible-builds")
    id("io.gitlab.arturbosch.detekt")
}

val libs = versionCatalogs.named("libs")

kotlin {
    jvm()

    withSourcesJar(publish = true)

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
    }

    sourceSets.commonMain.dependencies {
        implementation(dependencies.platform("com.kelvsyc.internal:platform"))
    }

    sourceSets.commonTest.dependencies {
        libs.findLibrary("kotest-assertions-core").getOrNull()?.let { implementation(it) }
        libs.findLibrary("kotest-assertions-shared").getOrNull()?.let { implementation(it) }
        libs.findLibrary("kotest-framework-api").getOrNull()?.let { implementation(it) }
        libs.findLibrary("kotest-framework-engine").getOrNull()?.let { implementation(it) }
        libs.findLibrary("kotest-runner").getOrNull()?.let { implementation(it) }
    }
}

jvmDependencyConflicts {
    logging {
        enforceSlf4JSimple()
    }
}

detekt {
    config.from(file("../../gradle/detekt.yml"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// The Kotlin Multiplatform Plugin doesn't integrate with the Test Report Aggregation Plugin
// So we kind of have to reconstruct it from scratch
@Suppress("UnstableApiUsage")
val testResultsElements = configurations.consumable("testResultsElementsForJvmTest") {
    // This configuration mimics that defined by the "test-suite-base" plugin
    description = "Binary results obtained from running the 'jvmTest' suites."

    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
    attributes.attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.TEST_RESULTS))
    // FIXME how do we accommodate multiple JVM targets and their respective test suites?
    attributes.attribute(TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE, objects.named("test"))

    val binaryDir = tasks.named<Test>("jvmTest").flatMap { it.binaryResultsDirectory }
    outgoing.artifact(binaryDir) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
}
