import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("S3 Java Base")
}

gradlePlugin {
    plugins.register("aws-s3-java-base") {
        id = "com.kelvsyc.gradle.aws-s3-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.S3JavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.s3.java)
    api(libs.aws.s3.transfer.manager.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
    implementation(libs.mockk)
}
