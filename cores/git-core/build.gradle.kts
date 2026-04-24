import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Git Core")
}

gradlePlugin {
    plugins.register("git-core") {
        id = "com.kelvsyc.gradle.git-core"
        implementationClass = "com.kelvsyc.gradle.plugins.GitCorePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.jgit)

    testImplementation(libs.mockk)
}
