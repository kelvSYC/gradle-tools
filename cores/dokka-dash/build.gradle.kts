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

    testImplementation(libs.mockk)
}
