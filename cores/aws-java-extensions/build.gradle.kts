plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-gradle-library")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("AWS Java Gradle Extensions")
    modulePath.set(project.name)
}

dependencies {
    implementation("com.kelvsyc.gradle:clients-base")

    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
}
