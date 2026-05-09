import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("ECR Kotlin Base")
}

gradlePlugin {
    plugins.register("aws-ecr-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-ecr-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.EcrKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.ecr.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}
