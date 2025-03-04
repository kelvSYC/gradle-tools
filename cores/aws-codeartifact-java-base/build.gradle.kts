plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("CodeArtifact Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("codeartifact-java-base") {
        id = "com.kelvsyc.gradle.codeartifact-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.CodeArtifactJavaBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.codeartifact.java)
}
