import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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

val libs = versionCatalogs.named("libs")
val tier = gradleTier()

// Published code compiles against its tier's Gradle API and Kotlin, not the Gradle running the build.
// `kotlin-dsl` and `java-gradle-plugin` add the running Gradle's API as file dependencies, and `kotlin-dsl` adds its
// embedded Kotlin, so main's compile classpath drops both.
configurations.named { it == "compileOnly" || it == "compileOnlyApi" }.configureEach {
    withDependencies { removeIf { it is FileCollectionDependency } }
}
configurations.compileOnly {
    setExtendsFrom(extendsFrom.filterNot { it.name == "embeddedKotlin" })
}

tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
        apiVersion.set(tier.kotlinVersion)
        languageVersion.set(tier.kotlinVersion)
    }
}

dependencies {
    implementation(platform("com.kelvsyc.internal:platform"))
    compileOnly("org.gradle.experimental:gradle-public-api:${tier.publicApiVersion}") { gradlePublicApi() }
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${tier.embeddedKotlin}")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:${tier.embeddedKotlin}")
    compileOnly(libs.findLibrary("jspecify").get())

    // Unit tests run against the Gradle running the build.
    testImplementation(gradleApi())
    testImplementation(gradleKotlinDsl())
    testImplementation(kotlin("stdlib"))

    libs.findLibrary("kotest-assertions-core").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-framework-engine").getOrNull()?.let { testImplementation(it) }
    libs.findLibrary("kotest-runner").getOrNull()?.let { testImplementation(it) }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    suppressedValidationErrors.add("dependencies-without-versions")
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
}
