import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.github-publishing")
    id("com.kelvsyc.internal.kotlin-multiplatform-jacoco")
    id("com.kelvsyc.internal.kotlin-multiplatform-jvm-library")
}

group = "com.kelvsyc.kotlin"

configure<DokkaExtension> {
    moduleName.set("Kotlin Core")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(libs.kotest.property)
        implementation(libs.mockk)
    }

    sourceSets.jvmMain.dependencies {
        implementation(libs.guava)
        implementation(libs.commons.lang)
        implementation(libs.commons.io)
    }
}
