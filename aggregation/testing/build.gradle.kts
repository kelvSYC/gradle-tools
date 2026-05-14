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
// When adding a new *-base component with a BuildService, also add it here and to the
// Integration Tests step in .github/workflows/gradle-build.yml.
val integrationTestComponents = setOf(
    "artifactory-base",
    "aws-imds-java-base",
    "aws-s3-java-base",
    "aws-secrets-manager-java-base",
    "aws-sns-java-base",
    "aws-sns-kotlin-base",
    "azure-blob-storage-base",
    "bitbucket-cloud-base",
    "bitbucket-data-center-base",
    "google-cloud-storage-base",
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
