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
