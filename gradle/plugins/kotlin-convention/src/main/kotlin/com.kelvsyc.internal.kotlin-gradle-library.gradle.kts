import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.optionals.getOrNull

plugins {
    `java-library`
    kotlin("jvm")

    id("com.autonomousapps.dependency-analysis")
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.reproducible-builds")
    id("io.gitlab.arturbosch.detekt")
}

dependencyAnalysis {
    issues {
        all {
            onUsedTransitiveDependencies {
                severity("fail")
                exclude(
                    "io.mockk:mockk-core",
                    "io.mockk:mockk-dsl",
                    "io.kotest:kotest-assertions-shared",
                    "io.kotest:kotest-common",
                )
            }
            onUnusedDependencies { severity("fail") }
            onIncorrectConfiguration { severity("fail") }
        }
    }
}

group = "com.kelvsyc.gradle"

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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

// Consumers always run inside Gradle, which supplies kotlin-stdlib. Publishing a version here would conflict with
// Gradle's Kotlin DSL plugins on older Gradle releases, which pin it strictly to their embedded Kotlin.
extra["kotlin.stdlib.default.dependency"] = "false"

val libs = versionCatalogs.named("libs")
val tier = gradleTier()

// Published variants require no Kotlin stdlib, and declare the oldest Gradle they support.
excludeKotlinStdlibFromPublishedDependencies()
declareGradleTier(tier)

// main compiles against the variants of its dependencies that a consumer on the tier's oldest Gradle resolves.
compileMainForGradleTier(tier)

// Published code compiles against its tier's Gradle API and Kotlin, not the Gradle running the build.
tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
        apiVersion.set(tier.kotlinVersion)
        languageVersion.set(tier.kotlinVersion)
        // Gradle 9.0's JSpecify annotations mark some results non-null that later releases mark nullable, such as
        // ValueSource.obtain(), and Kotlin enforces JSpecify strictly by default. Report these as warnings, as Kotlin
        // already does for the JSR-305 annotations of Gradle 8.14.
        if (tier == GradleTier.GRADLE_9_0) {
            freeCompilerArgs.add("-Xjspecify-annotations=warn")
        }
    }
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    compileOnly("org.gradle.experimental:gradle-public-api:${tier.publicApiVersion}") { gradlePublicApi() }
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${tier.embeddedKotlin}")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:${tier.embeddedKotlin}")
    compileOnly(libs.findLibrary("jspecify").get())

    // Unit tests compile and run against the Gradle running the build.
    testCompileOnly(gradleApi())
    testCompileOnly(gradleKotlinDsl())
    testImplementation(kotlin("stdlib"))
    testRuntimeOnly(gradleApi())
    testRuntimeOnly(gradleKotlinDsl())

    libs.findLibrary("kotest-assertions-core").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-runner").getOrNull()?.let { testImplementation(it) }
}

pluginManager.withPlugin("maven-publish") {
    extensions.configure<PublishingExtension> {
        publications.register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // mockk uses ByteBuddy dynamic agent loading; JVM 21+ warns and JVM 25 may deny it by default
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    // ByteBuddy only officially supports up to Java 24; experimental mode allows it to run on newer JVMs
    jvmArgs("-Dnet.bytebuddy.experimental=true")
    // Pre-attach the ByteBuddy agent at JVM startup — dynamic self-attachment is unreliable on JVM 25
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        classpath.find { "byte-buddy-agent" in it.name }
            ?.let { listOf("-javaagent:$it") }
            ?: emptyList()
    })
    // Gradle's ProjectBuilder needs java.lang open for class-loader injection on JDK 25+
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}
