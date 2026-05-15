import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Azure Managed Identity Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api(libs.azure.identity)
    api(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.moshi)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    testImplementation(libs.mockk)
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
