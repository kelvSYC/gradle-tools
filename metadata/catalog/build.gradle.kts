plugins {
    `version-catalog`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

val pluginIdPrefix = "com.kelvsyc.gradle"
val projectVersionAlias = "gradle-tools-version"

val coreNames = file("../../cores")
    .listFiles { f -> f.isDirectory && f.resolve("settings.gradle.kts").exists() }
    ?.toList()
    .orEmpty()
    .map { it.name }

val bomVersion = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--abbrev=0")
}.standardOutput.asText.map { it.trim().removePrefix("v") }

catalog {
    versionCatalog {
        version(projectVersionAlias, bomVersion.get())

        coreNames.forEach {
            if (it.endsWith("extensions")) {
                library(it, group.toString(), it).versionRef(projectVersionAlias)
            } else {
                library("$it-plugin", group.toString(), it).versionRef(projectVersionAlias)
                plugin(it, "$pluginIdPrefix.$it").versionRef(projectVersionAlias)
            }
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["versionCatalog"])
    }
}
