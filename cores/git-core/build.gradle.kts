plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Git Core")
    modulePath.set(project.name)
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
}
