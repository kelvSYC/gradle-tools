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
    // Gradle can only rank variants that all declare a version, including when the consumer requests no attributes.
    if (name in setOf("apiElements", "runtimeElements", "sourcesElements")) {
        attributes {
            attribute(GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("9.8"))
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

detekt {
    source.from("src/baseline/kotlin", "src/gradle99/kotlin")
}

fun ExternalModuleDependency.gradlePublicApi() {
    capabilities { requireCapability("org.gradle.experimental:gradle-public-api-internal") }
    // The API pins jspecify strictly, which clashes with the newer version Caffeine brings. Each variant
    // declares jspecify itself, so the API's nullness annotations stay visible to the compiler.
    exclude(group = "org.jspecify")
}

dependencies {
    "gradle99CompileOnly"("org.gradle.experimental:gradle-public-api:9.9.0") { gradlePublicApi() }
    "gradle99CompileOnly"(kotlin("reflect"))
    "gradle99CompileOnly"(libs.jspecify)
    "gradle99CompileOnly"(kotlin("stdlib"))
    "gradle99Implementation"(platform("com.kelvsyc.internal:platform"))
    "gradle99Implementation"(libs.caffeine)

    implementation(libs.caffeine)

    testCompileOnly(gradleApi())
    testCompileOnly(gradleKotlinDsl())

    testImplementation(gradleTestKit())
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}

val variantTestRepo = layout.buildDirectory.dir("variant-test-repo")

publishing {
    repositories.maven(variantTestRepo) { name = "variantTest" }
}

// GradleVariantSelectionSpec resolves a dynamic version, so the repository must hold exactly one release.
val cleanVariantTestRepo = tasks.register<Delete>("cleanVariantTestRepo") { delete(variantTestRepo) }
tasks.withType<PublishToMavenRepository>().matching { it.name.endsWith("ToVariantTestRepository") }.configureEach {
    dependsOn(cleanVariantTestRepo)
}

tasks.test {
    dependsOn("publishMavenPublicationToVariantTestRepository")
    val repo = variantTestRepo
    // maven-metadata.xml changes on every publish, so only track the artifacts themselves.
    inputs.files(repo.map { it.asFileTree.matching { include("**/*.jar", "**/*.module") } })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("variantTestArtifacts")
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-DvariantTest.repo=${repo.get().asFile.path}") })

    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
