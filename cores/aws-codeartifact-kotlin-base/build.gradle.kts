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
    implementation("com.kelvsyc.gradle:aws-kotlin-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.codeartifact.kotlin)
}
