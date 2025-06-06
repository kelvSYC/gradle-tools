group = "com.kelvsyc"

val components = buildList {
    // Gradle Plugin Libraries
    add("aws-java-extensions")
    add("aws-kotlin-extensions")
    add("gradle-extensions")

    // Gradle Plugins
    add("artifactory-base")
    add("aws-codeartifact-java-base")
    add("aws-codeartifact-kotlin-base")
    add("aws-imds-java-base")
    add("aws-imds-kotlin-base")
    add("aws-s3-java-base")
    add("aws-s3-kotlin-base")
    add("aws-secrets-manager-java-base")
    add("aws-secrets-manager-kotlin-base")
    add("aws-ses-java-base")
    add("aws-ses-kotlin-base")
    add("aws-sns-java-base")
    add("aws-sns-kotlin-base")
    add("aws-sqs-java-base")
    add("aws-sqs-kotlin-base")
    add("clients-base")
    add("git-core")
    add("google-cloud-artifact-registry-base")
    add("google-cloud-storage-base")

    // Kotlin Extension Libraries
    add("commons-lang-extensions")
    add("commons-numbers-extensions")
    add("guava-extensions")
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
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:$name"))
}

tasks.register("build") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":catalog:generateCatalogAsToml"))
    dependsOn(gradle.includedBuild("aggregation").task(":dokkatoo:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
    dependsOn(gradle.includedBuild("aggregation").task(":platform:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
}

tasks.register("publish") {
    components.forEach {
        dependsOn(gradle.includedBuild(it).task(":$name"))
    }
    dependsOn(gradle.includedBuild("aggregation").task(":catalog:$name"))
    dependsOn(gradle.includedBuild("aggregation").task(":platform:$name"))
}

tasks.register("dokkatooGenerate") {
    dependsOn(gradle.includedBuild("aggregation").task(":dokkatoo:dokkatooGenerate"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("aggregation").task(":testing:testAggregateTestReport"))
}

tasks.register("jacoco") {
    dependsOn(gradle.includedBuild("aggregation").task(":jacoco:testCodeCoverageReport"))
}
