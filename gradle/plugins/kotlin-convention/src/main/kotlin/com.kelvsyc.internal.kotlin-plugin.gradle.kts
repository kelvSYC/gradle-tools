import kotlin.jvm.optionals.getOrNull

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

detekt {
    config.from(file("../../gradle/detekt.yml"))
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))

    libs.findLibrary("kotest-assertions-core").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-assertions-shared").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-api").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-runner").getOrNull()?.let { testImplementation(it) }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
