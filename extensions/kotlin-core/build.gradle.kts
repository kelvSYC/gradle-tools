plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.github-publishing")
    id("com.kelvsyc.internal.kotlin-multiplatform-jvm-library")
}

group = "com.kelvsyc.kotlin"

dokkatoo {
    moduleName.set("Kotlin Core")
    modulePath.set(project.name)
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(libs.mockk)
    }
}
