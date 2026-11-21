import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
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
