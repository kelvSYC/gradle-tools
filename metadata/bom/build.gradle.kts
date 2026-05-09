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
    `java-platform`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

val pluginIdPrefix = "com.kelvsyc.gradle"

val coreNames = file("../../cores")
    .listFiles { f -> f.isDirectory && f.resolve("settings.gradle.kts").exists() }
    ?.toList()
    .orEmpty()
    .map { it.name }

val bomVersion = providers.of(GitDescribeVersion::class) {}.orElse("unspecified")

dependencies {
    constraints {
        coreNames.forEach {
            if (it.endsWith("extensions")) {
                api("$group:$it:${bomVersion.get()}")
            } else {
                val pluginId = "$pluginIdPrefix.$it"
                api("$group:$it:${bomVersion.get()}")
                api("$pluginId:$pluginId.gradle.plugin:${bomVersion.get()}")
            }
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["javaPlatform"])
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        require(bomVersion.get() != "unspecified") {
            "Cannot publish: no git tag found. Tag the repo with a 'v' prefix before publishing."
        }
    }
}
