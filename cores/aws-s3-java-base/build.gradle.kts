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
    plugins.register("aws-s3-java-base") {
        id = "com.kelvsyc.gradle.aws-s3-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.S3JavaBasePlugin"
    }
}

dependencies {
    api("com.kelvsyc.gradle:aws-java-extensions")
    api("com.kelvsyc.gradle:clients-base")

    api(libs.aws.s3.java)
    api(libs.aws.s3.transfer.manager.java)
    implementation(libs.aws.auth.java)
    implementation(libs.aws.core.java)
    implementation(libs.aws.regions.java)
}
