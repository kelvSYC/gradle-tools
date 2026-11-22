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
