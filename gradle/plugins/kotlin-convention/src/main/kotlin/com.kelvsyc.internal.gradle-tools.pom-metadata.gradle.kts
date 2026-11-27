/**
 * The POM metadata Maven Central-style consumers expect on every publication: name, description, project URL,
 * Apache-2.0 license, SCM and developer. Applied by every core (through `com.kelvsyc.internal.gradle-tools.core`) and by
 * the BOM and catalog builds.
 *
 * Covers every `MavenPublication`, including the plugin marker publications. A name or description that is already
 * set (Gradle names the markers itself) is kept.
 */

pluginManager.withPlugin("maven-publish") {
    val repositoryUrl = "https://github.com/kelvSYC/gradle-tools"

    the<PublishingExtension>().publications.withType<MavenPublication>().configureEach {
        pom {
            name.convention(project.name)
            project.description?.let { description.convention(it) }
            url.convention(repositoryUrl)
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/kelvSYC/gradle-tools.git")
                developerConnection.set("scm:git:ssh://git@github.com/kelvSYC/gradle-tools.git")
                url.set(repositoryUrl)
            }
            developers {
                developer {
                    id.set("kelvSYC")
                    name.set("Kelvin Chung")
                }
            }
        }
    }
}
