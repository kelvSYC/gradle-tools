import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("KMS Java Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.aws.kms.java)
    api(libs.aws.sdk.core.java)

    testImplementation(libs.mockk)
}
