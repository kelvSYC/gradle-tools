plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("S3 Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("s3-java-base") {
        id = "com.kelvsyc.gradle.s3-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.S3JavaBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.s3.java)
    api(libs.aws.s3.transfer.manager.java)
}
