import org.jetbrains.dokka.gradle.DokkaExtension
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
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
        externalDocumentationLinks.register("commons-lang") {
            url("https://commons.apache.org/proper/commons-lang/apidocs")
            packageListUrl("https://commons.apache.org/proper/commons-lang/apidocs/element-list")
        }
        externalDocumentationLinks.register("commons-numbers") {
            url("https://commons.apache.org/proper/commons-numbers/commons-numbers-docs/apidocs")
        }
        externalDocumentationLinks.register("guava") {
            url("https://javadoc.io/doc/com.google.guava/guava/latest")
            packageListUrl("https://javadoc.io/doc/com.google.guava/guava/latest/element-list")
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
