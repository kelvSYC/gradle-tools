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
    moduleName.set("Gitea Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.retrofit)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.moshi)

    testImplementation(libs.mockk)
}
