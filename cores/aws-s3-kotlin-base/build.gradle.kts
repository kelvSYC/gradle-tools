plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("S3 Kotlin Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("s3-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-s3-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.S3KotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.s3.kotlin)
    implementation(libs.aws.core.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.aws.smithy.runtime.core)
    implementation(libs.kotlinx.coroutines.core)
}
