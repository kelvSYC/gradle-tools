plugins {
    `java-platform`
}

group = "com.kelvsyc.internal"

javaPlatform {
    // Allow for dependencies on other platforms (including BOMs)
    allowDependencies()
}

dependencies {
    api(platform(libs.aws.java.sdk.bom))
    api(platform(libs.aws.kotlin.sdk.bom))
    api(platform(libs.commons.numbers.bom))
    api(platform(libs.google.cloud.libraries.bom))
    api(platform(libs.jackson.bom))
    api(platform(libs.kotest.bom))
    api(platform(libs.kotlin.gradle.plugins.bom))
    api(platform(libs.kotlinx.coroutines.bom))
    constraints {
        api(libs.commons.lang)
    }
}
