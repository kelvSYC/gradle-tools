import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Commons Numbers Extensions")
}

dependencies {
    api(libs.commons.numbers.complex)
    api(libs.commons.numbers.core)
    api(libs.commons.numbers.fraction)

    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}
