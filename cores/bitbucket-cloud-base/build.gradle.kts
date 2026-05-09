import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Bitbucket Cloud Base")
}

gradlePlugin {
    plugins.register("bitbucket-cloud-base") {
        id = "com.kelvsyc.gradle.bitbucket-cloud-base"
        implementationClass = "com.kelvsyc.gradle.plugins.BitbucketCloudBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.retrofit)
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.moshi)

    testImplementation(libs.mockk)
}
