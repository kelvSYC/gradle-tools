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
    moduleName.set("IMDS Kotlin Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.config.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.aws.smithy.http)
    testImplementation(libs.mockk)
}
