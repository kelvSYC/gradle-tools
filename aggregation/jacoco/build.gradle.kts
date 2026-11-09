import org.gradle.api.attributes.plugin.GradlePluginApiVersion

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

// This configuration requests no attributes, so Gradle cannot choose between per-Gradle-version variants.
configurations.aggregateCodeCoverageReportResults {
    attributes {
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(GradleVersion.current().version))
    }
}
