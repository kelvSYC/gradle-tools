plugins {
    `version-catalog`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

val projectVersionAlias = "gradle-tools-version"

val coreDirs = file("../../cores")
    .listFiles { f -> f.isDirectory && f.resolve("settings.gradle.kts").exists() }
    ?.toList()
    .orEmpty()
    .sortedBy { it.name }

val coreNames = coreDirs.map { it.name }

// Plugin registrations (registration name to plugin ID) declared by a core's `gradlePlugin { plugins.register(...) }`.
val pluginRegistration = Regex("""plugins\.register\("([^"]+)"\)\s*\{\s*id\s*=\s*"([^"]+)"""")

val pluginIds: Map<String, String> = coreDirs.flatMap { core ->
    val script = core.resolve("build.gradle.kts").takeIf { it.exists() }?.readText().orEmpty()
    val parsed = pluginRegistration.findAll(script).map { it.groupValues[1] to it.groupValues[2] }.toList()
    check(parsed.size == Regex("""plugins\.register\(""").findAll(script).count()) {
        "Could not read every plugin ID in ${core.name}/build.gradle.kts; expected `plugins.register(\"name\") { id = \"...\" }`"
    }
    parsed
}.toMap()

catalog {
    versionCatalog {
        version(projectVersionAlias, version.toString())

        coreNames.forEach {
            if (it.endsWith("extensions")) {
                library(it, group.toString(), it).versionRef(projectVersionAlias)
            } else {
                library("$it-plugin", group.toString(), it).versionRef(projectVersionAlias)
            }
        }
        pluginIds.forEach { (name, id) ->
            plugin(name, id).versionRef(projectVersionAlias)
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["versionCatalog"])
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    // Read at configuration time and held in a local: a script-level val would capture the script object in the action.
    val publishedVersion = version.toString()
    doFirst {
        require(publishedVersion != "unspecified") {
            "Cannot publish: no git tag found. Tag the repo with a 'v' prefix before publishing."
        }
    }
}
