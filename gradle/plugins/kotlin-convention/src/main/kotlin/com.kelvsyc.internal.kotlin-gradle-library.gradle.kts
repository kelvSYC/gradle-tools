plugins {
    `java-library`
    kotlin("jvm")

    id("com.autonomousapps.dependency-analysis")
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.reproducible-builds")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.kelvsyc.gradle"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    compileOnlyApi(gradleApi())
    compileOnlyApi(gradleKotlinDsl())
}
