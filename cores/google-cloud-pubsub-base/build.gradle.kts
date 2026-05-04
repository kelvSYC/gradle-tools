import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Pub/Sub Base")
}

gradlePlugin {
    plugins.register("google-cloud-pubsub-base") {
        id = "com.kelvsyc.gradle.google-cloud-pubsub-base"
        implementationClass = "com.kelvsyc.gradle.plugins.GoogleCloudPubSubBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.google.cloud.pubsub)

    testImplementation(libs.mockk)
}
