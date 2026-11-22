import kotlin.jvm.optionals.getOrNull

/**
 * Adds an `integrationTest` source set and matching `Test` task for TestKit-driven probes that exercise
 * Gradle's configuration cache and `WorkerExecutor` serialization boundaries against the host component's
 * `BuildService` and `WorkAction` types.
 *
 * Consumers apply this plugin alongside `com.kelvsyc.internal.kotlin-gradle-library`.
 *
 * The host component's runtime classpath plus the `integrationTest` source set output is exposed to the
 * `integrationTest` task as the `integration-test.host-classpath` system property, so spec code can emit a
 * `buildscript { dependencies { classpath(files(...)) } }` header into generated TestKit projects. This is
 * the workaround for the fact that the `*-base` modules are libraries of abstract types — they do not
 * register Gradle plugins, so `withPluginClasspath()` is not available.
 */

plugins {
    `java-library`
}

val integrationTest = sourceSets.register("integrationTest") {
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += output + compileClasspath
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations["testImplementation"])
}

configurations.named("integrationTestRuntimeOnly") {
    extendsFrom(configurations["testRuntimeOnly"])
}

val libs = versionCatalogs.named("libs")

dependencies {
    libs.findLibrary("kotest-assertions-core").getOrNull()
        ?.let { add("integrationTestImplementation", it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()
        ?.let { add("integrationTestImplementation", it) }
    libs.findLibrary("kotest-runner").getOrNull()
        ?.let { add("integrationTestImplementation", it) }
    add("integrationTestImplementation", gradleTestKit())
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests exercising Gradle configuration cache " +
            "and WorkerExecutor serialization boundaries via TestKit."
    group = "verification"
    testClassesDirs = integrationTest.get().output.classesDirs
    classpath = integrationTest.get().runtimeClasspath
    shouldRunAfter("test")
    // Spec code reads this and emits a buildscript classpath into generated TestKit projects so the host
    // component's `BuildService`, `WorkAction` and synthetic fixture types are visible to the daemon.
    val hostClasspath = files(sourceSets["main"].runtimeClasspath, integrationTest.get().output)
    inputs.files(hostClasspath).withNormalizer(ClasspathNormalizer::class)
    doFirst {
        systemProperty("integration-test.host-classpath", hostClasspath.asPath)
    }
}

tasks.named("check") {
    dependsOn(integrationTestTask)
}
