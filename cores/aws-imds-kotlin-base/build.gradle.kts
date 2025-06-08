plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("IMDS Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("aws-imds-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-imds-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ImdsKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.config.kotlin)
}
