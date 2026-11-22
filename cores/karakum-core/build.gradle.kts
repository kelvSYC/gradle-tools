import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("com.kelvsyc.internal.dokka")
    id("com.kelvsyc.internal.gradle-tools.jacoco")
    id("com.kelvsyc.internal.kotlin-gradle-plugin")
    id("com.kelvsyc.internal.gradle-tools.core")
    id("com.kelvsyc.internal.gradle-tools.detekt")
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
