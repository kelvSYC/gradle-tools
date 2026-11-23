pluginManagement {
    includeBuild("../settings")
    // Gradle resolves the included settings plugin's external dependencies with this build's plugin repositories.
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kelvSYC/gradle-build-conventions") {
            name = "GitHubPackagesBuildConventions"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

plugins {
    id("com.kelvsyc.internal.gradle-tools.settings")
}

dependencyResolutionManagement {
    // The convention plugins depend on Gradle plugins (detekt, gradlex and others), resolved from the Plugin Portal.
    repositories.gradlePluginPortal()
}

include("kotlin-convention")
