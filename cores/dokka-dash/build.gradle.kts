import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
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
    // The version tests read this directly, so without it a new nightly would be served from the build cache.
    inputs.property("additionalGradleVersions", providers.environmentVariable("ADDITIONAL_GRADLE_VERSIONS").orElse(""))
}
