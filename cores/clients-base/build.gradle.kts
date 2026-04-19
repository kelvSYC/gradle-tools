import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
    alias(libs.plugins.gradle.testkit.jacoco)
}

configure<DokkaExtension> {
    moduleName.set("Clients Base")
}

gradlePlugin {
    plugins.register("clients-base") {
        id = "com.kelvsyc.gradle.clients-base"
        implementationClass = "com.kelvsyc.gradle.plugins.ClientsBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'
}
