dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
    }
}
