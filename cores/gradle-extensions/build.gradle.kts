import org.gradle.api.attributes.java.TargetJvmEnvironment.STANDARD_JVM
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.api.attributes.plugin.GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Gradle Extensions")
}

// One artifact serves several Gradle versions: Gradle selects the variant for the version it is running.
// Shared code in src/main/kotlin is compiled into every variant, against that variant's Gradle API.
sourceSets.main { kotlin.srcDir("src/baseline/kotlin") }
val gradle99 = sourceSets.create("gradle99") { kotlin.srcDir("src/main/kotlin") }

java {
    registerFeature(gradle99.name) {
        usingSourceSet(gradle99)
        withSourcesJar()
        capability(project.group.toString(), project.name, project.version.toString())
    }
}

configurations.configureEach {
    if (isCanBeConsumed && name.startsWith(gradle99.name)) {
        attributes {
            attribute(GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("9.9"))
        }
    }
    if (name == gradle99.apiElementsConfigurationName || name == gradle99.runtimeElementsConfigurationName) {
        // Match what KGP sets on apiElements/runtimeElements, or Gradle cannot rank the variants.
        attributes {
            attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(STANDARD_JVM))
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
        }
    }
}

// This library compiles against specific Gradle API versions, not the Gradle running the build.
configurations.compileOnlyApi {
    withDependencies { removeIf { it is FileCollectionDependency } }
}

fun ExternalModuleDependency.gradlePublicApi() {
    capabilities { requireCapability("org.gradle.experimental:gradle-public-api-internal") }
    // The API pins jspecify strictly, which clashes with the newer version Caffeine brings.
    exclude(group = "org.jspecify")
}

dependencies {
    // Deliberately pinned: the oldest Gradle version this library supports.
    compileOnly("org.gradle.experimental:gradle-public-api:9.8.2") { gradlePublicApi() }
    // Provided by Gradle at runtime; previously came from gradleKotlinDsl().
    compileOnly(kotlin("reflect"))

    "gradle99CompileOnly"("org.gradle.experimental:gradle-public-api:9.9.0") { gradlePublicApi() }
    "gradle99CompileOnly"(kotlin("reflect"))
    "gradle99Api"(kotlin("stdlib"))
    "gradle99Implementation"(platform("com.kelvsyc.internal:platform"))
    "gradle99Implementation"(libs.caffeine)

    implementation(libs.caffeine)

    testCompileOnly(gradleApi())
    testCompileOnly(gradleKotlinDsl())

    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
