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
    moduleName.set("S3 Kotlin Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.aws.s3.kotlin)
    implementation(libs.aws.core.kotlin)
    implementation(libs.aws.smithy.runtime.core)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}
