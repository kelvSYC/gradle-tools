plugins {
    `java-platform`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

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

dependencies {
    constraints {
        coreNames.forEach {
            api("$group:$it:$version")
        }
        pluginIds.values.forEach { pluginId ->
            api("$pluginId:$pluginId.gradle.plugin:$version")
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["javaPlatform"])
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
