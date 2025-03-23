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
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.auth.java)
    api(libs.aws.core.java)
    api(libs.aws.regions.java)
}
