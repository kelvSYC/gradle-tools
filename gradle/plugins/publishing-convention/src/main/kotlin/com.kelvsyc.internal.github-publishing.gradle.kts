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
}
