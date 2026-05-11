import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Azure Blob Storage Core")
}

gradlePlugin {
    plugins.register("azure-blob-storage-base") {
        id = "com.kelvsyc.gradle.azure-blob-storage-base"
        implementationClass = "com.kelvsyc.gradle.plugins.AzureBlobStorageBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.azure.core)
    api(libs.azure.storage.blob)

    testImplementation(libs.mockk)
}
