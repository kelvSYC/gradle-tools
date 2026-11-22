plugins {
    id("jacoco-report-aggregation")
}

group = "com.kelvsyc.gradle"

val coreComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
dependencies {
    coreComponents.forEach {
        jacocoAggregation("$group:${it.name}") // from included build $it.name
    }
}

reporting {
    reports.register<JacocoCoverageReport>("testCodeCoverageReport") {
        testSuiteName.set("test")
    }
}

// The aggregate coverage report built from execution data and classes already on disk, without depending on any test
// task. CI runs the root `check` in shards, copies every shard's results into place and runs this (through the root
// `mergeShardReports`). It covers every core's main classes; unlike testCodeCoverageReport, which analyses gradle-extensions'
// gradle99 variant, it analyses gradle-extensions' main (baseline) classes, the ones its tests run against.
tasks.register<JacocoReport>("mergeShardCoverageReport") {
    description = "Builds the aggregate coverage report from the cores' execution data on disk, without running tests."
    group = "verification"
    val coreDirs = coreComponents.map { it.projectDir }.sortedBy { it.name }
    executionData.from(coreDirs.map { it.resolve("build/jacoco/test.exec") })
    classDirectories.from(coreDirs.flatMap { listOf(it.resolve("build/classes/kotlin/main"), it.resolve("build/classes/java/main")) })
    // Every non-test source set, as some cores add source directories to main from elsewhere under src.
    sourceDirectories.from(coreDirs.flatMap { core ->
        core.resolve("src").listFiles().orEmpty().filterNot { it.name.contains("test", ignoreCase = true) }.sorted()
            .flatMap { listOf(it.resolve("kotlin"), it.resolve("java")) }
    })
    reports {
        xml.required = true
        html.required = true
    }
}
