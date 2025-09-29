import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

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
            // FIXME replace 'main' with git commit ref
            remoteUrl("https://github.com/kelvSYC/gradle-tools/blob/main/$relativePath")
        }
    }
}

pluginManager.withPlugin("java") {
    configure<DokkaExtension> {
        dokkaSourceSets.configureEach {
            // Set the JDK version as the default toolchain used
            jdkVersion.set(the<JavaPluginExtension>().toolchain.languageVersion.map { it.asInt() })
            jdkVersion.convention(21) // In case toolchain settings are not set
        }
    }
}
