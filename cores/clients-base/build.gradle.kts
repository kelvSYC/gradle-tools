plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Clients Base")
    modulePath.set(project.name)
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
