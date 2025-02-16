plugins {
    id("com.kelvsyc.internal.kotlin-plugin")
}

gradlePlugin {
    plugins.register("git-core") {
        id = "com.kelvsyc.gradle.git-core"
        implementationClass = "com.kelvsyc.gradle.plugins.GitCorePlugin"
    }
}

dependencies {
    api(libs.jgit)
}
