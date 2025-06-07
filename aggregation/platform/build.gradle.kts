plugins {
    `java-platform`
    id("com.kelvsyc.internal.github-publishing")
}

group = "com.kelvsyc.gradle"

val pluginIdPrefix = "com.kelvsyc.gradle"

dependencies {
    constraints {
        val cores = gradle.includedBuilds.filter {
            it.projectDir.parentFile.name == "cores"
        }

        cores.forEach {
            if (it.name.endsWith("extensions")) {
                // Core libraries
                val baseModule = "$group:${it.name}"

                api("$baseModule:$version")
            } else {
                // Core plugins
                val baseModule = "$group:${it.name}"
                val pluginId = "$pluginIdPrefix.${it.name}"
                val pluginModule = "$pluginId:$pluginId.gradle.plugin"

                api("$baseModule:$version")
                api("$pluginModule:$version")
            }
        }

        val extensionComponents = gradle.includedBuilds.filter {
            it.projectDir.parentFile.name == "extensions"
        }
        extensionComponents.forEach {
            val baseModule = "$group:${it.name}"

            api("$baseModule:$version")
        }
    }
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["javaPlatform"])
    }
}
