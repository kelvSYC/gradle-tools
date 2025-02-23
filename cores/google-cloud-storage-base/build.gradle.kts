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
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.google.cloud.storage)
}
