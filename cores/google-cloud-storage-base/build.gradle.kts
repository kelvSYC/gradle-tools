plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Google Cloud Storage Core")
    modulePath.set(project.name)
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
