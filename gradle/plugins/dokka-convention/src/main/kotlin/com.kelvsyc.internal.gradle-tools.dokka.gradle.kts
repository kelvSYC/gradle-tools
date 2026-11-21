import kotlin.jvm.optionals.getOrNull
import org.jetbrains.dokka.gradle.DokkaExtension
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

val libs = versionCatalogs.named("libs")

// Dokka's own internal resolver configurations (not derived from `implementation`/`api`, so the
// `com.kelvsyc.internal:platform` BOM's constraints never reach them) pull in a vulnerable transitive
// jsoup via dokka-base/analysis-markdown, and a vulnerable transitive jackson-bom (with its
// jackson-core/jackson-databind/etc.) directly off dokka-base/dokka-core. These configurations are
// resolver-role-locked (dependencies can't be declared against them directly), so the versions must
// be forced via resolution strategy instead of importing a BOM - forcing the BOM module itself isn't
// enough, since Gradle's automatic virtual-platform alignment across the jackson-* family doesn't
// reliably cascade in every project (e.g. `aggregation:dokka`, whose generator classpath merges
// dependency edges from every included `cores/*` build via the `dokka(...)` cross-build dependency).
val jsoupVersion = libs.findLibrary("jsoup").getOrNull()?.map { it.versionConstraint.requiredVersion }
val jacksonVersion = libs.findLibrary("jackson-bom").getOrNull()?.map { it.versionConstraint.requiredVersion }
configurations.matching { it.name.startsWith("dokka") }.configureEach {
    resolutionStrategy.eachDependency {
        // jackson-annotations dropped its patch version segment in the 2.20 line, so it never
        // matches the rest of the family's version string; leave it to resolve against the other
        // forced modules' own (correct) dependency metadata instead.
        if (requested.group.startsWith("com.fasterxml.jackson") && requested.name != "jackson-annotations") {
            jacksonVersion?.let { useVersion(it.get()) }
        }
        if (requested.group == "org.jsoup" && requested.name == "jsoup") {
            jsoupVersion?.let { useVersion(it.get()) }
        }
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
            remoteUrl.set(gitCommitHash.map { URI("https://github.com/kelvSYC/gradle-tools/blob/$it/$relativePath") })
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
