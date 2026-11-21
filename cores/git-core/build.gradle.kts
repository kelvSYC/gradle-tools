import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
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
    api("com.kelvsyc.gradle:clients-base")
    api(libs.jgit)

    testImplementation(libs.mockk)
}
