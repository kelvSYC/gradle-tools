import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
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
    testImplementation(libs.mockk)
}
