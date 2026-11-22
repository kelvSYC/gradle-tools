import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.gradle-tools.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.gradle-tools.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.github-publishing")
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
