import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Secret Manager Base")
}

gradlePlugin {
    plugins.register("google-cloud-secret-manager-base") {
        id = "com.kelvsyc.gradle.google-cloud-secret-manager-base"
        implementationClass = "com.kelvsyc.gradle.plugins.GoogleCloudSecretManagerBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.google.cloud.secret.manager)

    testImplementation(libs.mockk)
}
