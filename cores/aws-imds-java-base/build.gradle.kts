import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("IMDS Java Base")
}

gradlePlugin {
    plugins.register("aws-imds-java-base") {
        id = "com.kelvsyc.gradle.aws-imds-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ImdsJavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.imds.java)

    testImplementation(libs.mockk)
}
