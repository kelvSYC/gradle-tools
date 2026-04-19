import com.javiersc.semver.settings.gradle.plugin.SemverSettingsExtension

// Settings plugin to be applied to all builds located in the cores directory
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
