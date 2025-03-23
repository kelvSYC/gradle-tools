plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Artifactory Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("artifactory-base") {
        id = "com.kelvsyc.gradle.artifactory-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ArtifactoryBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.artifactory.client.api)
    implementation(libs.artifactory.client.impl)
}
