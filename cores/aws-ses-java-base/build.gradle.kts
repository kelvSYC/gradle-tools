import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("SES Java Base")
}

gradlePlugin {
    plugins.register("aws-ses-java-base") {
        id = "com.kelvsyc.gradle.aws-ses-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SesJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.ses.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)

    testImplementation(libs.mockk)
}
