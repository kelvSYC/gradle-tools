plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("SES Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("ses-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-ses-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SesKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.ses.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)
}
