import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Dokka Dash Docset Plugin")
}

gradlePlugin {
    plugins.register("dokka-dash") {
        id = "com.kelvsyc.gradle.dokka-dash"
        implementationClass = "com.kelvsyc.gradle.plugins.DokkaDashPlugin"
    }
}

dependencies {
    implementation(libs.gson)

    testImplementation(gradleTestKit())
    testImplementation(libs.mockk)
    testImplementation(libs.sqlite.jdbc)
}

tasks.test {
    // One DokkaDashGradleVersionSpec subclass per Gradle version (com.kelvsyc.gradle.plugins.gradleversions), each in its
    // own test JVM, so that every version's distribution download, daemon start and dependency resolution overlap.
    maxParallelForks = 4
    // Each version's TestKit directory is a subdirectory. Not an input.
    val testKitDir = layout.buildDirectory.dir("gradle-version-testkit")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-DgradleVersionTest.testKitDir=${testKitDir.get().asFile.path}")
    })
}
