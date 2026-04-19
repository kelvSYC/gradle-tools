import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Storage Core")
}

gradlePlugin {
    plugins.register("google-cloud-storage-base") {
        id = "com.kelvsyc.gradle.google-cloud-storage-base"
        implementationClass = "com.kelvsyc.gradle.plugins.GoogleCloudStorageBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.google.cloud.storage)
}
