plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Secrets Manager Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("secrets-manager-java-base") {
        id = "com.kelvsyc.gradle.aws-secrets-manager-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SecretsManagerJavaBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.secrets.manager.java)
    api(libs.aws.secrets.manager.caching.java)
}
