group = "com.kelvsyc"

val components = buildList {
    // Gradle Plugin Libraries
    add("gradle-extensions")

    // Gradle Plugins
    add("clients-base")
    add("git-core")
}

tasks.register("clean") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":platform:$name"))
}

tasks.register("assemble") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":dokkatoo:$name"))
}

tasks.register("build") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":catalog:generateCatalogAsToml"))
    dependsOn(gradle.includedBuild("aggregation").task(":platform:$name"))
}

tasks.register("publish") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":catalog:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":platform:$name"))
}
