plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("CodeArtifact Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("aws-codeartifact-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-codeartifact-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.CodeArtifactKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.codeartifact.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.aws.smithy.runtime.core)
    implementation(libs.kotlinx.coroutines.core)
}
