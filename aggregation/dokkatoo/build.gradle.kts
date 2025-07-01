plugins {
    alias(libs.plugins.dokkatoo.html)
}

group = "com.kelvsyc.gradle"

val cores = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
val extensionComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "extensions"
}

dokkatoo {
    moduleName.set("kelvSYC Gradle Tools")
}

dependencies {
    cores.forEach {
        dokkatoo("$group:${it.name}") // from included build $it.name
    }
    extensionComponents.forEach {
        if (it.name == "kotlin-core") {
            // FIXME Placeholder due to kotlin-core using a different group ID
            dokkatoo("com.kelvsyc.kotlin:kotlin-core")
        } else {
            dokkatoo("$group:${it.name}") // from included build $it.name
        }
    }
}

tasks.assemble {
    dependsOn(tasks.dokkatooGeneratePublicationHtml)
}
