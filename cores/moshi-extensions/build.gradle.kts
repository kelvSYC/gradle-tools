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
    moduleName.set("Moshi Extensions")
}

dependencies {
    api(local.kotlin.tools.moshi.extensions)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)

    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}
