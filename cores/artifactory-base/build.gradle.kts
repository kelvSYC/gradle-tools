import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Artifactory Base")
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
