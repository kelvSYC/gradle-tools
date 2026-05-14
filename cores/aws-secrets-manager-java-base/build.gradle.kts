import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-integration-test")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Secrets Manager Java Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.aws.secrets.manager.java)
    api(libs.aws.secrets.manager.caching.java)

    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
