import kotlin.jvm.optionals.getOrNull
import org.jetbrains.dokka.gradle.DokkaExtension
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

val libs = versionCatalogs.named("libs")

// Dokka's own internal resolver configurations (not derived from `implementation`/`api`, so the
// `com.kelvsyc.internal:platform` BOM's constraints never reach them) pull in a vulnerable transitive
// jsoup via dokka-base/analysis-markdown, and a vulnerable transitive jackson-bom 2.15.3 (with its
// jackson-core/jackson-databind/etc.) directly off dokka-base/dokka-core; force the patched versions
// on just those configurations.
val jsoup = libs.findLibrary("jsoup").getOrNull()
val jacksonBom = libs.findLibrary("jackson-bom").getOrNull()
configurations.matching { it.name.startsWith("dokka") }.configureEach {
    resolutionStrategy {
        jsoup?.let { force(it.get()) }
        // Force the BOM module itself rather than pinning every individual jackson-* artifact to
        // the same literal version string: jackson-annotations doesn't always ship a patch release
        // in lockstep with jackson-core/jackson-databind (e.g. 2.22 exists but 2.22.1 doesn't), so
        // the BOM's own recommended versions must decide each artifact's actual resolved version.
        jacksonBom?.let { force(it.get()) }
    }
}

val gitCommitHash: Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "HEAD")
}.standardOutput.asText.map { it.trim() }

configure<DokkaExtension> {
    // Can't use Gradle.rootGradle from gradle-extensions due to circular shenanigans
    val rootGradle = generateSequence(gradle, Gradle::getParent).last()
    val relativePath = layout.projectDirectory.asFile
        .toRelativeString(rootGradle.rootProject.layout.projectDirectory.asFile)

    dokkaSourceSets.configureEach {
        enableJdkDocumentationLink.set(true)
        enableKotlinStdLibDocumentationLink.set(true)

        externalDocumentationLinks.register("gradle") {
            url("https://docs.gradle.org/current/kotlin-dsl/gradle")
        }
        sourceLink {
            remoteUrl.set(gitCommitHash.map { URI("https://github.com/kelvSYC/rifflet/blob/$it/$relativePath") })
        }
    }
}

pluginManager.withPlugin("java") {
    apply(plugin = "org.jetbrains.dokka-javadoc")
    configure<DokkaExtension> {
        dokkaSourceSets.configureEach {
            jdkVersion.convention(
                project.the<JavaPluginExtension>().toolchain.languageVersion.map { it.asInt() }.orElse(25)
            )
        }
    }
    configure<JavaPluginExtension> {
        withJavadocJar()
    }
    tasks.named<Jar>("javadocJar") {
        from(tasks.named("dokkaGeneratePublicationJavadoc"))
    }
}

tasks.named("assemble") {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
}
