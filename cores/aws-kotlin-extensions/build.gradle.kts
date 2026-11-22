import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("AWS Kotlin Gradle Extensions")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.smithy.client)
    api(libs.aws.smithy.credentials)
    implementation(libs.aws.config.kotlin)
    implementation(libs.aws.smithy.runtime.core)

    testImplementation(libs.mockk)
}
