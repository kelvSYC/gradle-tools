group = "com.kelvsyc"

val components = file("cores").listFiles { dir -> dir.isDirectory && dir.resolve("settings.gradle.kts").exists() }?.map { it.name }.orEmpty()

tasks.register("clean") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
}

tasks.register("assemble") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":dokka:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:$name"))
}

tasks.register("build") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":catalog:generateCatalogAsToml"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
}

tasks.register("publish") {
    group = "publishing"
    description = "Publish all components"

    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("metadata").task(":catalog:$name"))
    dependsOn(gradle.includedBuild("metadata").task(":bom:$name"))
}

tasks.register("dokkaGenerate") {
    dependsOn(gradle.includedBuild("aggregation").task(":dokka:build"))
}

tasks.register("detekt") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
}

tasks.register("check") {
    dependsOn("test")
    dependsOn("detekt")
}

tasks.register("jacoco") {
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
}
