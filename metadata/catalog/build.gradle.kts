// providers.exec fails eagerly on non-zero exit with no way to ignore it, so CI
// shallow clones without tags blow up during configuration — even for unrelated
// tasks like `help`. A custom ValueSource lets us return null on failure and fall
// back to "unspecified", deferring the real check to publish time.
abstract class GitDescribeVersion : ValueSource<String, ValueSourceParameters.None> {
    override fun obtain(): String? = try {
        val process = ProcessBuilder("git", "describe", "--tags", "--match", "v*", "--abbrev=0")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) output.removePrefix("v") else null
    } catch (_: Exception) {
        null
    }
}

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

val bomVersion = providers.of(GitDescribeVersion::class) {}.orElse("unspecified")

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

tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        require(bomVersion.get() != "unspecified") {
            "Cannot publish: no git tag found. Tag the repo with a 'v' prefix before publishing."
        }
    }
}
