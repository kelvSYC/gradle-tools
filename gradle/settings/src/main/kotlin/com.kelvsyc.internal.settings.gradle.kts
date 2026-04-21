import com.javiersc.semver.settings.gradle.plugin.SemverSettingsExtension

pluginManagement {
    includeBuild("../../gradle/plugins")
}

plugins {
    id("com.javiersc.semver")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    includeBuild("../../gradle/platform")

    versionCatalogs.register("libs") {
        from(files("../../gradle/libs.versions.toml"))
    }
}

configure<SemverSettingsExtension> {
    isEnabled.set(true)
    gitDir.set(layout.settingsDirectory.dir("../../.git"))
}
