import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

configure<DokkaExtension> {
    moduleName.set("Karakum Core")
}

gradlePlugin {
    plugins.register("karakum-core") {
        id = "com.kelvsyc.gradle.karakum-core"
        implementationClass = "com.kelvsyc.gradle.plugins.KarakumPlugin"
    }
}

dependencies {
    compileOnly(libs.kotlin.plugin)

    testImplementation(libs.kotlin.plugin)
    testImplementation(libs.mockk)
}
