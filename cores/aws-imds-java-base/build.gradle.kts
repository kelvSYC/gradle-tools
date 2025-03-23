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
    plugins.register("aws-imds-java-base") {
        id = "com.kelvsyc.gradle.aws-imds-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ImdsJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.imds.java)
}
