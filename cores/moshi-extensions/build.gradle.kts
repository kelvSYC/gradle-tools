import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Moshi Extensions")
}

dependencies {
    api(local.kotlin.tools.moshi.extensions)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)

    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
