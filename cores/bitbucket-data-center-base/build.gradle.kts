import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Bitbucket Data Center Base")
}

gradlePlugin {
    plugins.register("bitbucket-data-center-base") {
        id = "com.kelvsyc.gradle.bitbucket-data-center-base"
        implementationClass = "com.kelvsyc.gradle.plugins.BitbucketDataCenterBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.moshi)

    testImplementation(libs.mockk)
}
