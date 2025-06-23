plugins {
    jacoco
    `java-library`
}

jacoco {
    version = "0.8.13" // FIXME Get from version catalog
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
    }
}
