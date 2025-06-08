plugins {
    `version-catalog`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

val pluginIdPrefix = "com.kelvsyc.gradle"
val projectVersionAlias = "gradle-tools-version"

catalog {
    versionCatalog {
        version(projectVersionAlias, version.toString())

        val cores = gradle.includedBuilds.filter {
            it.projectDir.parentFile.name == "cores"
        }

        cores.forEach {
            if (it.name.endsWith("extensions")) {
                // Core libraries
                library(it.name, group.toString(), it.name).versionRef(projectVersionAlias)
            } else {
                // Core plugins
                library("${it.name}-plugin", group.toString(), it.name).versionRef(projectVersionAlias)
                plugin(it.name, "$pluginIdPrefix.${it.name}").versionRef(projectVersionAlias)
            }
        }

        val extensionComponents = gradle.includedBuilds.filter {
            it.projectDir.parentFile.name == "extensions"
        }
        extensionComponents.forEach {
            library(it.name, group.toString(), it.name).versionRef(projectVersionAlias)
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["versionCatalog"])
    }
}
