pluginManagement {
    includeBuild("../../gradle/settings")
}

plugins {
    id("com.kelvsyc.internal")
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.gradle.org/gradle/libs-releases") {
            content { includeGroup("org.gradle.experimental") }
        }
    }
}
