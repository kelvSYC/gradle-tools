import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Google Cloud Run Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:google-cloud-extensions")
    api(libs.google.cloud.run)
    api(libs.google.gax)
    implementation(libs.google.cloud.run.proto)
    implementation(libs.google.protobuf.java)
    testImplementation(libs.mockk)
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
