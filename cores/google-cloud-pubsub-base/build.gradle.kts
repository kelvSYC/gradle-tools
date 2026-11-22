import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Pub/Sub Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:google-cloud-extensions")

    api(libs.google.cloud.pubsub)
    api(libs.google.gax)
    implementation(libs.google.cloud.pubsub.proto)
    implementation(libs.google.protobuf.java)

    testImplementation(libs.mockk)
}
