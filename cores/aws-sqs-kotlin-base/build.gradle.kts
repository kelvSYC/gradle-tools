plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("SQS Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("sQs-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-sqs-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SqsKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.sqs.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)
}
