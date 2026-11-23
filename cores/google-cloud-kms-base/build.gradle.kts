import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud KMS Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:google-cloud-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.google.auth.library.credentials)
    api(libs.google.cloud.kms)
    api(libs.google.gax)
    implementation(libs.google.protobuf.java)

    testImplementation(libs.mockk)
}
