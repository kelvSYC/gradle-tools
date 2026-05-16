import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
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
