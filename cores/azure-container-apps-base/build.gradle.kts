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
    moduleName.set("Azure Container Apps Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:azure-extensions")
    api(libs.azure.core)
    api(libs.azure.resourcemanager.appcontainers)

    testImplementation(libs.mockk)
}

tasks.test {
    // Mock build services are compiled into the test source set but no runnable specs exist yet
    failOnNoDiscoveredTests = false
}
