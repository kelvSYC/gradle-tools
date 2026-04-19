plugins {
    id("com.kelvsyc.internal.dokka")
}

group = "com.kelvsyc.gradle"

val cores = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
val extensionComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "extensions"
}

dokka {
    moduleName.set("kelvSYC Gradle Tools")
}

dependencies {
    cores.forEach {
        dokka("$group:${it.name}") // from included build ${it.name}
    }
    extensionComponents.forEach {
        if (it.name == "kotlin-core") {
            // FIXME Placeholder due to kotlin-core using a different group ID
            dokka("com.kelvsyc.kotlin:kotlin-core")
        } else {
            dokka("$group:${it.name}") // from included build ${it.name}
        }
    }
}
