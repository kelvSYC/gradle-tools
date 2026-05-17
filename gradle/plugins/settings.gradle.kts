import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kelvSYC/kotlin-tools") {
            name = "GitHubPackages"
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    includeBuild("../platform")

    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
        // Add entry for the `kotlin-dsl` plugin, using the expectedKotlinDslPluginsVersion constant
        library("kotlin-dsl-plugins", "org.gradle.kotlin", "gradle-kotlin-dsl-plugins")
            .version(expectedKotlinDslPluginsVersion)
    }
}

include("dokka-convention")
include("jacoco-convention")
include("kotlin-convention")
include("publishing-convention")
include("semver-convention")
