import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Lambda Java Base")
}

gradlePlugin {
    plugins.register("aws-lambda-java-base") {
        id = "com.kelvsyc.gradle.aws-lambda-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.LambdaJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.lambda.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
    implementation(libs.aws.sdk.core.java)

    testImplementation(libs.mockk)
}
