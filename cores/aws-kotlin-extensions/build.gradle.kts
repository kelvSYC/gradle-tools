plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("AWS Kotlin Gradle Extensions")
    modulePath.set(project.name)
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    implementation(libs.aws.config.kotlin)
    api(libs.aws.smithy.client)
    api(libs.aws.smithy.credentials)
}
