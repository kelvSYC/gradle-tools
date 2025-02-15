plugins {
    `java-platform`
}

group = "com.kelvsyc.internal"

javaPlatform {
    // Allow for dependencies on other platforms (including BOMs)
    allowDependencies()
}

dependencies {
    api(platform(libs.kotlin.gradle.plugins.bom))
    constraints {

    }
}
