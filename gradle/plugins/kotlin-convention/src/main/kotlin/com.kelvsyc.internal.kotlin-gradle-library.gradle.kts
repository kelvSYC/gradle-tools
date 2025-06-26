import kotlin.jvm.optionals.getOrNull

plugins {
    `java-library`
    kotlin("jvm")

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

jvmDependencyConflicts {
    logging {
        enforceSlf4JSimple()
    }
}

detekt {
    config.from(file("../../gradle/detekt.yml"))
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    compileOnlyApi(gradleApi())
    compileOnlyApi(gradleKotlinDsl())
    testRuntimeOnly(gradleApi())
    testRuntimeOnly(gradleKotlinDsl())

    libs.findLibrary("kotest-assertions-core").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-assertions-shared").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-api").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-runner").getOrNull()?.let { testImplementation(it) }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
