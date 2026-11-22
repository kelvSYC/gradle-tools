import org.jetbrains.dokka.gradle.DokkaExtension

/**
 * What every core adds to the shared conventions: gradlex dependency conflict resolution (with slf4j-simple as the
 * logging implementation) and reproducible builds, a link to the Gradle API in its Dokka documentation, the compat
 * repository that aggregation/compat resolves the cores from.
 *
 * Applied after the shared kind (`com.kelvsyc.internal.kotlin-gradle-library` or `-plugin`), which applies
 * `java-library`.
 */

plugins {
    `java-library`
    id("org.gradlex.jvm-dependency-conflict-resolution")
    id("org.gradlex.reproducible-builds")
}

jvmDependencyConflicts {
    logging {
        enforceSlf4JSimple()
    }
}

// Every core compiles against the Gradle API.
pluginManager.withPlugin("org.jetbrains.dokka") {
    configure<DokkaExtension> {
        dokkaSourceSets.configureEach {
            externalDocumentationLinks.register("gradle") {
                url("https://docs.gradle.org/current/kotlin-dsl/gradle")
            }
        }
    }
}

// aggregation/compat resolves cores from build/compat-repo on each tier's oldest Gradle. The repository is not one of
// the publishing repositories, so `publish` stays the GitHub Packages release path: each publication gets its own
// `publish<Name>PublicationToCompatRepository` task instead.
pluginManager.withPlugin("maven-publish") {
    val compatRepositoryDir = layout.settingsDirectory.dir("../../build/compat-repo")
    // Created through the project's repository handler, then removed from it again: dependencies never resolve from it.
    // This relies on the settings allowing project repositories (no FAIL_ON_PROJECT_REPOS or PREFER_SETTINGS mode).
    val compatRepository = repositories.maven(compatRepositoryDir) { name = "compat" }
    repositories.remove(compatRepository)

    // The compat tests resolve each core at a dynamic version, so the repository must hold exactly one release of it.
    val compatModuleDir = compatRepositoryDir.dir(provider { "${group.toString().replace('.', '/')}/${project.name}" })
    val cleanCompatRepository = tasks.register<Delete>("cleanCompatRepository") {
        delete(compatModuleDir)
    }

    the<PublishingExtension>().publications.withType<MavenPublication>().configureEach {
        val publication = this
        tasks.register<PublishToMavenRepository>("publish${name.replaceFirstChar(Char::uppercase)}PublicationToCompatRepository") {
            group = "publishing"
            description = "Publishes Maven publication '${publication.name}' to the compat repository."
            this.publication = publication
            repository = compatRepository
            dependsOn(cleanCompatRepository)
        }
    }
}
