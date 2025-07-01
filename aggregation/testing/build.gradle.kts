plugins {
    `jvm-ecosystem`
    `test-report-aggregation`
}

group = "com.kelvsyc.gradle"

val coreComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "cores"
}
val extensionComponents = gradle.includedBuilds.filter {
    it.projectDir.parentFile.name == "extensions"
}

dependencies {
    coreComponents.forEach {
        testReportAggregation("$group:${it.name}") // from included build $it.name
    }
    extensionComponents.forEach {
        if (it.name == "kotlin-core") {
            // FIXME Placeholder due to kotlin-core using a different group ID
            testReportAggregation("com.kelvsyc.kotlin:kotlin-core")
        } else {
            testReportAggregation("$group:${it.name}") // from included build $it.name
        }
    }
}

reporting {
    reports.register<AggregateTestReport>("testAggregateTestReport") {
        testSuiteName.set("test")
    }
}
