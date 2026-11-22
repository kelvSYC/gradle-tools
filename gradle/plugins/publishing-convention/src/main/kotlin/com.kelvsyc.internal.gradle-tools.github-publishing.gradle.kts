import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    `maven-publish`
}

publishing {
    repositories.maven("https://maven.pkg.github.com/kelvSYC/gradle-tools") {
        name = "GitHubPackages"
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom.withXml {
            StripInternalPlatform.fromPom(asNode())
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    // Versions come from the published BOM. Without kotlin-stdlib, many components publish no version of their own.
    suppressedValidationErrors.add("dependencies-without-versions")
    doLast(StripInternalPlatform.ModuleMetadataAction())
}

// Cores also publish to a local repository that aggregation's compat project resolves them from, on each tier's
// oldest Gradle. Other builds applying this plugin (metadata) live elsewhere, so the relative path does not fit them.
if (layout.settingsDirectory.asFile.parentFile.name == "cores") {
    publishing.repositories.maven(layout.settingsDirectory.dir("../../build/compat-repo")) {
        name = "compat"
    }

    // The compat tests resolve each core at a dynamic version, so the repository must hold exactly one release of it.
    val compatModuleDir = layout.settingsDirectory.dir(
        provider { "../../build/compat-repo/${group.toString().replace('.', '/')}/${project.name}" }
    )
    val cleanCompatRepository = tasks.register<Delete>("cleanCompatRepository") {
        delete(compatModuleDir)
    }
    tasks.withType<PublishToMavenRepository>().matching { it.name.endsWith("ToCompatRepository") }.configureEach {
        dependsOn(cleanCompatRepository)
    }
}
