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

dependencies {
    constraints {
        coreNames.forEach {
            if (it.endsWith("extensions")) {
                api("$group:$it:$version")
            } else {
                val pluginId = "$pluginIdPrefix.$it"
                api("$group:$it:$version")
                api("$pluginId:$pluginId.gradle.plugin:$version")
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
        require(version != "unspecified") {
            "Cannot publish: no git tag found. Tag the repo with a 'v' prefix before publishing."
        }
    }
}
