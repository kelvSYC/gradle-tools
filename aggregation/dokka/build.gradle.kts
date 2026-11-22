plugins {
    id("com.kelvsyc.internal.dokka")
}

group = "com.kelvsyc.gradle"

val cores = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
dokka {
    moduleName.set("kelvSYC Gradle Tools")
}

dependencies {
    cores.forEach {
        dokka("$group:${it.name}") // from included build ${it.name}
    }
}
