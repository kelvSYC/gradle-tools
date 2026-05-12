import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("STS Kotlin Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.sts.kotlin)
    api(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}
