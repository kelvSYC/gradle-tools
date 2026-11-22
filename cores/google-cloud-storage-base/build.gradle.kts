import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.gradle-tools.gradle-integration-test")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Storage Core")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:google-cloud-extensions")

    api(libs.google.auth.library.credentials)
    api(libs.google.auth.library.oauth2.http)
    api(libs.google.cloud.storage)
    api(libs.google.cloud.core)

    testImplementation(libs.mockk)
}
