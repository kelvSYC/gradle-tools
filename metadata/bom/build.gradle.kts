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

val bomVersion = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--abbrev=0")
}.standardOutput.asText.map { it.trim().removePrefix("v") }

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
