plugins {
    id("com.kelvsyc.internal.dokkatoo")
    id("com.kelvsyc.internal.kotlin-plugin")
    id("com.kelvsyc.internal.github-publishing")
}

dokkatoo {
    moduleName.set("SNS Java Base")
    modulePath.set(project.name)
}

gradlePlugin {
    plugins.register("sns-java-base") {
        id = "com.kelvsyc.gradle.sns-java-base"
        implementationClass = "com.kelvsyc.gradle.plugins.SnsJavaBasePlugin"
    }
}

dependencies {
    implementation("com.kelvsyc.gradle:aws-java-extensions")
    implementation("com.kelvsyc.gradle:clients-base")
    implementation("com.kelvsyc.gradle:gradle-extensions") // build 'gradle-extensions'

    api(libs.aws.auth.java)
    api(libs.aws.regions.java)
    api(libs.aws.sns.java)
}
