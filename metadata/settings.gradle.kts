pluginManagement {
    includeBuild("../gradle/plugins")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.github.com/kelvSYC/kotlin-tools") {
            name = "GitHubPackages"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

plugins {
    id("com.kelvsyc.internal.semver")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.github.com/kelvSYC/kotlin-tools") {
            name = "GitHubPackages"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    includeBuild("../gradle/platform")

    versionCatalogs.register("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}

include("bom")
include("catalog")
