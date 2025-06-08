plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("CodeArtifact Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("aws-codeartifact-java-base") {
        id = "com.kelvsyc.gradle.aws-codeartifact-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.CodeArtifactJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.codeartifact.java)
    implementation(libs.aws.core.java)
}
