plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
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
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.secrets.manager.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)
}
