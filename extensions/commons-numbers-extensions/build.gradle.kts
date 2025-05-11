plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-library")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("Commons Numbers Extensions")
    modulePath.set(project.name)
}

dependencies {
    api(libs.commons.numbers.core)
    api(libs.commons.numbers.fraction)

    testImplementation(libs.mockk)
}
