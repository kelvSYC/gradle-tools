import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Artifact Registry Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.google.cloud.artifact.registry)
    api(libs.google.cloud.artifact.registry.proto)
    api(libs.google.gax)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
