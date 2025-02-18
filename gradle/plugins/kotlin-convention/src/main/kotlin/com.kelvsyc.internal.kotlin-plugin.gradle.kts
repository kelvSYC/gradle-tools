plugins {
    `java-library`
    // FIXME https://github.com/gradle/gradle/issues/22428 - can't use `kotlin-dsl` due to it containing a version
    id("org.gradle.kotlin.kotlin-dsl")

    id("com.autonomousapps.dependency-analysis")
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.reproducible-builds")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.kelvsyc.gradle"

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
}
