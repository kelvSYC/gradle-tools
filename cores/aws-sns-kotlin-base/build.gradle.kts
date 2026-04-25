import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("SNS Kotlin Base")
}

gradlePlugin {
    plugins.register("sns-kotlin-base") {
        id = "com.kelvsyc.gradle.aws-sns-kotlin-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SnsKotlinBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-kotlin-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.sns.kotlin)
    implementation(libs.aws.smithy.client)
    implementation(libs.aws.smithy.credentials)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
}
