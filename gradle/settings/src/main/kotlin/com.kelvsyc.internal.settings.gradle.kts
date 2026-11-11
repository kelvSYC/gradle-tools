pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kelvSYC/kotlin-tools") {
            name = "GitHubPackages"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    includeBuild("../../gradle/plugins")
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

    includeBuild("../../gradle/platform")

    versionCatalogs.register("libs") {
        from(files("../../gradle/libs.versions.toml"))
    }
}

// Cores compile against org.gradle.experimental:gradle-public-api, which only Gradle's own repository hosts.
// Declared here rather than in a project convention: a project-level repository would hide the ones above.
val gradleLibsReleases = java.net.URI("https://repo.gradle.org/gradle/libs-releases")
gradle.settingsEvaluated {
    dependencyResolutionManagement.repositories {
        if (none { it is MavenArtifactRepository && it.url == gradleLibsReleases }) {
            maven(gradleLibsReleases) {
                content { includeGroup("org.gradle.experimental") }
            }
        }
    }
}
