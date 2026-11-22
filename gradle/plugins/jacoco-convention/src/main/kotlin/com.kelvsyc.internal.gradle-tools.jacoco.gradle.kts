plugins {
    jacoco
    `java-library`
}

jacoco {
    toolVersion = "0.8.13" // FIXME Get from version catalog
}

// Tests still record execution data (build/jacoco/test.exec), which CI merges into one report across every core
// (:mergeShardReports); the per-core report is built only when asked for (./gradlew :<core>:jacocoTestReport).

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
    }
}
