import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-library")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("CodeArtifact Kotlin Base")
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    implementation("com.kelvsyc.gradle:gradle-extensions")

    api(libs.aws.codeartifact.kotlin)
    implementation(libs.aws.core.kotlin)
    implementation(libs.aws.smithy.runtime.core)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}

tasks.test {
    // FIXME https://github.com/gradle/gradle/issues/18647
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
