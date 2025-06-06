plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Google Cloud Artifact Registry Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("google-cloud-artifact-registry-base") {
        id = "com.kelvsyc.gradle.google-cloud-artifact-registry-base"
        implementationClass = "com.kelvsyc.gradle.plugins.GoogleCloudArtifactRegistryBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.google.cloud.artifact.registry)
    implementation(libs.kotlinx.coroutines.core)
}
