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
