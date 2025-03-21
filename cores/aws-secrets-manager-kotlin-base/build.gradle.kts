plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Secrets Manager Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("secrets-manager-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-secrets-manager-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SecretsManagerKotlinBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.secrets.manager.kotlin)
}
