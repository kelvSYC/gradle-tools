import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("JFrog CLI Core")
}

gradlePlugin {
    plugins.register("jfrog-cli-core") {
        id = "com.kelvsyc.gradle.jfrog-cli-core"
        implementationClass = "com.kelvsyc.gradle.plugins.JFrogCliPlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:clients-base")

    testImplementation(libs.mockk)
}
