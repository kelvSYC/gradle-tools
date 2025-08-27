dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    includeBuild("../platform")

    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
    }
}
