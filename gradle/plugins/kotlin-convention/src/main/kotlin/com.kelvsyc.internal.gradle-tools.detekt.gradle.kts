/**
 * Detekt for a core, with the configuration in gradle/detekt.yml.
 *
 * Detekt 1.23's bundled Kotlin compiler cannot parse the version strings of newer JDKs such as 25, so its tasks analyse
 * for JVM 22 with JDK 21's class library.
 */

plugins {
    `java-base`
    id("io.gitlab.arturbosch.detekt")
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
