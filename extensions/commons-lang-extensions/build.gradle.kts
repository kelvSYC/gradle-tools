import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Commons Lang Extensions")
}

dependencies {
    api(libs.commons.lang)
    implementation(kotlin("reflect"))

    testImplementation(libs.mockk)
}
