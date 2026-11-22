import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
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
