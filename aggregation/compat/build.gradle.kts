plugins {
    // The repository's Kotlin equals the wrapper's embedded Kotlin.
    kotlin("jvm") version embeddedKotlinVersion
}

// Not published, and distinct from the cores' group so that nothing aggregating cores picks this project up.
group = "com.kelvsyc.internal"

kotlin {
    jvmToolchain(21)
}

// Cores the consumers in TierCompatibilitySpec resolve, with every core they depend on.
val compatCores = listOf(
    "clients-base",
    "nexus-base",
    "aws-kotlin-extensions",
    "aws-sts-kotlin-base",
)

val compatRepo = layout.settingsDirectory.dir("../build/compat-repo")

// Published cores leave versions of third-party dependencies to BOMs, so consumers import these platforms.
val consumerPlatforms = listOf(
    libs.okhttp.bom,
    libs.retrofit.bom,
    libs.aws.kotlin.sdk.bom,
    libs.kotlinx.coroutines.bom,
).map { bom -> bom.map { "${it.module}:${it.versionConstraint.requiredVersion}" } }

dependencies {
    testImplementation(platform("com.kelvsyc.internal:platform"))
    testImplementation(gradleTestKit())
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.runner)
}

tasks.test {
    useJUnitPlatform()
    compatCores.forEach {
        dependsOn(gradle.includedBuild(it).task(":publishMavenPublicationToCompatRepository"))
    }
    val repo = compatRepo
    val platforms = consumerPlatforms
    // maven-metadata.xml changes on every publish, so only track the artifacts themselves.
    inputs.files(repo.asFileTree.matching { include("**/*.jar", "**/*.module") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("compatArtifacts")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Dcompat.repo=${repo.asFile.path}",
            "-Dcompat.platforms=${platforms.joinToString(",") { it.get() }}",
        )
    })
}
