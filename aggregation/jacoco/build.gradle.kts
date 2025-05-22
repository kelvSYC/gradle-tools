plugins {
    id("jacoco-report-aggregation")
}

group = "com.kelvsyc.gradle"

val extensionComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "extensions"
}

dependencies {
    extensionComponents.forEach {
        jacocoAggregation("$group:${it.name}") // from included build $it.name
    }
}

reporting {
    reports.register<JacocoCoverageReport>("testCodeCoverageReport") {
        testSuiteName.set("test")
    }
}
