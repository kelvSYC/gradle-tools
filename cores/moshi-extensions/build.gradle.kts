import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Moshi Extensions")
}

dependencies {
    api(libs.kotlin.tools.moshi.extensions)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)

    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
}

tasks.test {
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
