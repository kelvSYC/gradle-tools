import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("KMS Java Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.aws.kms.java)
    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.sdk.core.java)

    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
