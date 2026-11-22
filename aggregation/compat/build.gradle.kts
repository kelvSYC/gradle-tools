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
    "gradle-extensions",
    "aws-java-extensions",
    "aws-sts-java-base",
    "nexus-base",
    "aws-kotlin-extensions",
    "aws-sts-kotlin-base",
)

val compatRepo = layout.settingsDirectory.dir("../build/compat-repo")

// Consumers import these platforms, as consumers that manage versions with the third-party BOMs do.
val consumerPlatforms = listOf(
    libs.okhttp.bom,
    libs.retrofit.bom,
    libs.aws.java.sdk.bom,
    libs.aws.kotlin.sdk.bom,
    libs.kotlinx.coroutines.bom,
).map { bom -> bom.map { "${it.module}:${it.versionConstraint.requiredVersion}" } }

dependencies {
    testImplementation(platform(libs.conventions.platform))
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
    // One spec class per Gradle version, each in its own test JVM, so that every version's distribution download, daemon
    // start and dependency resolution overlap. Gradle caps the forks at max-workers (4 on CI's 4 vCPUs), so a fifth version
    // would share a JVM with another there.
    maxParallelForks = 4
    // TestKit's distributions and dependency caches live outside the build directory when CI caches them. Not an input.
    val testKitDir = providers.environmentVariable("COMPAT_TESTKIT_DIR")
        .orElse(layout.buildDirectory.dir("compat-testkit").map { it.asFile.path })
    // maven-metadata.xml changes on every publish, so only track the artifacts themselves.
    inputs.files(repo.asFileTree.matching { include("**/*.jar", "**/*.module") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("compatArtifacts")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Dcompat.repo=${repo.asFile.path}",
            "-Dcompat.testKitDir=${testKitDir.get()}",
            "-Dcompat.platforms=${platforms.joinToString(",") { it.get() }}",
        )
    })
}
