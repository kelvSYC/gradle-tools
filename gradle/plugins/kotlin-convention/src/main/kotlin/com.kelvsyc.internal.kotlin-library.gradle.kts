import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
        languageVersion.set(KotlinVersion.KOTLIN_2_3)
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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "22"
    jdkHome.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }.map { it.metadata.installationPath })
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))

    libs.findLibrary("kotest-assertions-core").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-assertions-shared").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-runner").getOrNull()?.let { testImplementation(it) }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // mockk uses ByteBuddy dynamic agent loading; JVM 21+ warns and JVM 25 may deny it by default
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    // Pre-attach the ByteBuddy agent at JVM startup — dynamic self-attachment is unreliable on JVM 25
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        classpath.find { "byte-buddy-agent" in it.name }
            ?.let { listOf("-javaagent:$it") }
            ?: emptyList()
    })
}
