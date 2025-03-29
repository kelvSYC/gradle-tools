plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("SQS Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("aws-sqs-java-base") {
        id = "com.kelvsyc.gradle.aws-sns-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SqsJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.sqs.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
}
