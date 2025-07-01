@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.github-publishing")
    alias(libs.plugins.kotlin.multiplatform)
}

group = "com.kelvsyc.kotlin"

dokkatoo {
    moduleName.set("Kotlin Core")
    modulePath.set(project.name)
}

kotlin {
    jvm()

    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}
