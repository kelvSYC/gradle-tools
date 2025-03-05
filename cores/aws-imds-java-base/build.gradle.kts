plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("IMDS Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("imds-java-base") {
        id = "com.kelvsyc.gradle.imds-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ImdsJavaBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.imds.java)
}
