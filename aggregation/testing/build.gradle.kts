plugins {
    `jvm-ecosystem`
    `test-report-aggregation`
}

group = "com.kelvsyc.gradle"

val coreComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
dependencies {
    coreComponents.forEach {
        testReportAggregation("$group:${it.name}") // from included build $it.name
    }
}

reporting {
    reports.register<AggregateTestReport>("testAggregateTestReport") {
        testSuiteName.set("test")
    }
}
