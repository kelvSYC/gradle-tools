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
    moduleName.set("AWS Java Gradle Extensions")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.auth.java)
    api(libs.aws.core.java)
    api(libs.aws.regions.java)

    testImplementation(libs.mockk)
}
