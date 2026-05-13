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

// Integration test reports (from components that apply `com.kelvsyc.internal.gradle-integration-test`) are
// surfaced separately as a copy task. The `test-report-aggregation` plugin's variant resolution does not
// gracefully handle a testsuite name that only some dependencies expose, so we collect the raw JUnit XML
// files directly from each opted-in core. CI globs the same locations independently for JUnit annotation.
val integrationTestComponents = setOf(
    "aws-sns-java-base",
    "google-cloud-storage-base",
    "azure-blob-storage-base",
)

val collectIntegrationTestReports = tasks.register<Sync>("collectIntegrationTestReports") {
    description = "Collects integration-test JUnit XML reports from opted-in cores."
    group = "verification"
    coreComponents
        .filter { it.name in integrationTestComponents }
        .forEach { included ->
            from(included.projectDir.resolve("build/test-results/integrationTest")) {
                into(included.name)
                include("TEST-*.xml")
            }
        }
    destinationDir = layout.buildDirectory.dir("reports/tests/integrationTest/raw").get().asFile
}

@Suppress("UnusedPrivateProperty")
val integrationTestReportArtifacts = collectIntegrationTestReports
