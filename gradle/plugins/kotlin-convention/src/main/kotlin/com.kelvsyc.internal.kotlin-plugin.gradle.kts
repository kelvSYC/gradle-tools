import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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
}
