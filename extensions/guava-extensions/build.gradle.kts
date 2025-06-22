plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.jacoco")
    id("com.kelvsyc.internal.kotlin-library")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Guava Extensions")
    modulePath.set(project.name)
}

dependencies {
    api(libs.guava)
    testImplementation(libs.mockk)
}
