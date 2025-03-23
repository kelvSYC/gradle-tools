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
    plugins.register("aws-secrets-manager-java-base") {
        id = "com.kelvsyc.gradle.aws-secrets-manager-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SecretsManagerJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.secrets.manager.java)
    api(libs.aws.secrets.manager.caching.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
}
