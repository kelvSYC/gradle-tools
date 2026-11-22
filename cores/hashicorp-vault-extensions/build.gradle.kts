import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("HashiCorp Vault Extensions")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api(libs.vault.java.driver)
    testImplementation(libs.mockk)
}

tasks.test {
    // Required for ProjectBuilder / MockK on JDK 25
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
